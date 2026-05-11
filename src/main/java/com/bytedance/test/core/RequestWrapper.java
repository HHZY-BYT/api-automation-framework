package com.bytedance.test.core;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * 统一请求封装类
 * 所有接口请求统一走这里，后续加Token、签名、超时、重试只改这一个类
 */
public class RequestWrapper {

    /**
     * 获取基础请求对象（继承BaseTest的全局配置）
     */
    private static RequestSpecification getRequestSpec() {
        return RestAssured.given();
    }

    /**
     * GET请求
     * @param url 接口地址
     * @return 响应对象
     */
    public static Response get(String url) {
        return getRequestSpec()
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    /**
     * GET请求（带路径参数）
     * @param url 接口地址
     * @param pathParams 路径参数
     * @return 响应对象
     */
    public static Response get(String url, Map<String, Object> pathParams) {
        return getRequestSpec()
                .pathParams(pathParams)
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    /**
     * POST请求（带请求体）
     * @param url 接口地址
     * @param body 请求体（POJO对象或Map）
     * @return 响应对象
     */
    public static Response post(String url, Object body) {
        return getRequestSpec()
                .body(body)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    /**
     * PUT请求（带请求体 + 路径参数）
     */
    public static Response put(String url, Object body, Map<String, Object> pathParams) {
        return getRequestSpec()
                .pathParams(pathParams)
                .body(body)
                .when()
                .put(url)
                .then()
                .extract()
                .response();
    }
    /**
     * DELETE请求（带路径参数）
     * @param url 接口地址
     * @param pathParams 路径参数
     * @return 响应对象
     */
    public static Response delete(String url, Map<String, Object> pathParams) {
        return getRequestSpec()
                .pathParams(pathParams)
                .when()
                .delete(url)
                .then()
                .extract()
                .response();
    }
}