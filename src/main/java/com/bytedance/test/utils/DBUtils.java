package com.bytedance.test.utils;

import com.bytedance.test.config.ConfigManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作工具类
 * 自动管理数据库连接，支持增删改查
 */
public class DBUtils {

    /**
     * 获取数据库连接
     */
    private static Connection getConnection() {
        try {
            Class.forName(ConfigManager.getDbDriver());
            return DriverManager.getConnection(
                    ConfigManager.getDbUrl(),
                    ConfigManager.getDbUsername(),
                    ConfigManager.getDbPassword()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取数据库连接失败", e);
        }
    }

    /**
     * 执行查询语句，返回List<Map>
     * @param sql SQL语句
     * @param params SQL参数
     * @return 查询结果
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置SQL参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            // 执行查询
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 处理结果集
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    resultList.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("执行查询失败：" + sql, e);
        }

        return resultList;
    }

    /**
     * 执行查询语句，返回单个结果
     * @param sql SQL语句
     * @param params SQL参数
     * @return 单行结果
     */
    public static Map<String, Object> executeQuerySingle(String sql, Object... params) {
        List<Map<String, Object>> resultList = executeQuery(sql, params);
        if (resultList.isEmpty()) {
            return null;
        }
        return resultList.get(0);
    }

    /**
     * 执行更新语句（INSERT/UPDATE/DELETE）
     * @param sql SQL语句
     * @param params SQL参数
     * @return 受影响的行数
     */
    public static int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置SQL参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            // 执行更新
            return pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("执行更新失败：" + sql, e);
        }
    }

    /**
     * 查询记录数
     * @param sql SQL语句
     * @param params SQL参数
     * @return 记录数
     */
    public static int count(String sql, Object... params) {
        List<Map<String, Object>> result = executeQuery(sql, params);
        if (result.isEmpty()) {
            return 0;
        }
        return ((Number) result.get(0).values().iterator().next()).intValue();
    }
}