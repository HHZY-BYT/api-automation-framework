package com.bytedance.test.utils;

import com.bytedance.test.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Token 管理工具类
 * 支持自动刷新、线程安全、过期检测
 */
public class TokenUtils {
    private static final Logger logger = LoggerFactory.getLogger(TokenUtils.class);

    private static volatile String token;
    private static volatile long tokenExpireTime;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Token 提前刷新时间（秒），避免临界点过期
    private static final long REFRESH_BEFORE_EXPIRE = 300;

    /**
     * 获取有效 Token（自动刷新）
     */
    public static String getToken() {
        lock.readLock().lock();
        try {
            if (isTokenValid()) {
                return token;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (!isTokenValid()) {
                refreshToken();
            }
            return token;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查 Token 是否有效
     */
    private static boolean isTokenValid() {
        if (token == null || token.isEmpty()) {
            return false;
        }
        if (tokenExpireTime == 0) {
            return true;
        }
        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime < (tokenExpireTime - REFRESH_BEFORE_EXPIRE);
    }

    /**
     * 刷新 Token（从登录接口获取）
     */
    private static void refreshToken() {
        try {
            logger.info("开始刷新 Token...");

            String loginUrl = ConfigManager.getLoginUrl();
            String username = ConfigManager.getLoginUsername();
            String password = ConfigManager.getLoginPassword();

            if (loginUrl == null || loginUrl.isEmpty()) {
                logger.warn("登录URL未配置，使用 Mock Token");
                token = generateMockToken();
                tokenExpireTime = 0;
                return;
            }

            Map<String, Object> loginBody = new HashMap<>();
            loginBody.put("username", username);
            loginBody.put("password", password);

            Response response = RestAssured.given()
                    .contentType("application/json;charset=UTF-8")
                    .body(loginBody)
                    .when()
                    .post(loginUrl)
                    .then()
                    .extract()
                    .response();

            int statusCode = response.getStatusCode();
            if (statusCode == 200 || statusCode == 201) {
                token = response.jsonPath().getString("token");

                if (token == null || token.isEmpty()) {
                    throw new RuntimeException("登录响应中未找到 token 字段");
                }

                Integer expiresIn = response.jsonPath().getInt("expires_in");
                if (expiresIn != null) {
                    tokenExpireTime = System.currentTimeMillis() / 1000 + expiresIn;
                    logger.info("Token 刷新成功，有效期: {} 秒", expiresIn);
                } else {
                    tokenExpireTime = 0;
                    logger.info("Token 刷新成功（无过期时间）");
                }
            } else {
                logger.warn("登录接口返回非成功状态码: {}，使用 Mock Token", statusCode);
                token = generateMockToken();
                tokenExpireTime = 0;
            }
        } catch (Exception e) {
            logger.warn("Token 刷新失败: {}，使用 Mock Token", e.getMessage());
            token = generateMockToken();
            tokenExpireTime = 0;
        }
    }

    /**
     * 生成 Mock Token（用于测试或登录接口不可用时）
     */
    private static String generateMockToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock.token." + System.currentTimeMillis();
    }

    /**
     * 强制刷新 Token（外部调用）
     */
    public static void forceRefreshToken() {
        lock.writeLock().lock();
        try {
            logger.info("强制刷新 Token...");
            token = null;
            tokenExpireTime = 0;
            refreshToken();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取认证 Header 的 Key
     */
    public static String getAuthHeaderKey() {
        return "Authorization";
    }

    /**
     * 获取认证 Header 的 Value
     */
    public static String getAuthHeaderValue() {
        return "Bearer " + getToken();
    }

    /**
     * 清除 Token（用于登出或测试清理）
     */
    public static void clearToken() {
        lock.writeLock().lock();
        try {
            token = null;
            tokenExpireTime = 0;
            logger.info("Token 已清除");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
