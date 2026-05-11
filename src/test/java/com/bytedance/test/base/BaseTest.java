package com.bytedance.test.base;

import com.bytedance.test.config.ConfigManager;
import com.bytedance.test.utils.TokenUtils;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigManager.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // 全局请求头 + 自动带Token
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType("application/json;charset=UTF-8")
                .setAccept("application/json")
                .addHeader(TokenUtils.getAuthHeaderKey(), TokenUtils.getAuthHeaderValue())
                .build();

        RestAssured.filters(
                new AllureRestAssured(),
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        );
    }
}