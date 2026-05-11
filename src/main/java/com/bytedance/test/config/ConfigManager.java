package com.bytedance.test.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();
    // 支持命令行指定环境：mvn test -Denv=test
    private static final String environment = System.getProperty("env", "dev");

    static {
        loadConfig();
    }

    private static void loadConfig() {
        String configFile = "config/" + environment + ".properties";
        try (InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(configFile)) {
            if (inputStream == null) {
                throw new RuntimeException("配置文件不存在: " + configFile);
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件失败", e);
        }
    }
    public static String getBaseUrl() {
        return get("base.url");
    }

    public static String getLoginUrl() {
        return get("login.url");
    }

    public static String getLoginUsername() {
        return get("login.username");
    }

    public static String getLoginPassword() {
        return get("login.password");
    }
    public static String get(String key) {
        return properties.getProperty(key);
    }


    // 读取数据库URL
    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    // 读取数据库用户名
    public static String getDbUsername() {
        return properties.getProperty("db.username");
    }

    // 读取数据库密码
    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }

    // 读取数据库驱动类名
    public static String getDbDriver() {
        return properties.getProperty("db.driver");
    }
}