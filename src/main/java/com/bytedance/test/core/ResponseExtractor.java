package com.bytedance.test.core;

import io.restassured.response.Response;

/**
 * 统一响应提取工具类
 * 封装所有常用的响应提取方法，统一断言标准
 */
public class ResponseExtractor {

    /**
     * 获取响应状态码
     */
    public static int getStatusCode(Response response) {
        return response.getStatusCode();
    }

    /**
     * 提取JSON字段（支持JSONPath）
     * @param response 响应对象
     * @param jsonPath JSON路径表达式
     * @return 提取的值
     */
    public static <T> T extract(Response response, String jsonPath) {
        return response.jsonPath().get(jsonPath);
    }

    /**
     * 提取响应头
     */
    public static String getHeader(Response response, String headerName) {
        return response.getHeader(headerName);
    }

    /**
     * 获取完整响应体字符串
     */
    public static String getResponseBody(Response response) {
        return response.getBody().asString();
    }

    /**
     * 将响应体转为指定POJO对象
     */
    public static <T> T as(Response response, Class<T> clazz) {
        return response.as(clazz);
    }
}