package com.bytedance.test.utils;

import io.restassured.response.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 自定义通用断言工具类
 * 统一封装常用断言，简化用例代码
 */
public class AssertUtils {

    // 断言状态码
    public static void assertStatusCode(Response response, int code) {
        assertThat(response.getStatusCode(), equalTo(code));
    }

    // 断言JSON字段等于指定值
    public static void assertJsonFieldEqual(Response response, String jsonPath, Object expect) {
        assertThat(response.jsonPath().get(jsonPath), equalTo(expect));
    }

    // 断言字段不为空
    public static void assertJsonFieldNotNull(Response response, String jsonPath) {
        assertThat(response.jsonPath().get(jsonPath), notNullValue());
    }

    // 断言包含字符串
    public static void assertBodyContains(Response response, String content) {
        assertThat(response.getBody().asString(), containsString(content));
    }
}