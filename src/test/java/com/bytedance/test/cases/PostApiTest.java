package com.bytedance.test.cases;

import com.bytedance.test.base.BaseTest;
import com.bytedance.test.core.RequestWrapper;
import com.bytedance.test.core.ResponseExtractor;
import com.bytedance.test.pojo.Post;
import com.bytedance.test.utils.AssertUtils;
import com.bytedance.test.utils.DBUtils;
import com.bytedance.test.utils.ExcelDataProvider;
import com.bytedance.test.utils.TokenUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * 帖子接口测试类
 * 继承 BaseTest 获取全局配置（如 Token、请求头、日志过滤器等）
 * 使用 Allure 注解生成测试报告
 */
@Feature("帖子接口模块")
public class PostApiTest extends BaseTest {

    /**
     * 测试 Token 自动获取功能
     * 验证 TokenUtils.getToken() 能够正常获取非空的 Token
     */
    @Test
    @Story("Token自动获取")
    @Description("验证能够正常获取Token")
    public void testGetToken() {
        // 调用 TokenUtils 获取 Token（自动处理缓存和刷新）
        String token = TokenUtils.getToken();
        
        // 断言 Token 不为 null
        assertNotNull(token, "Token 不应为 null");
        // 断言 Token 不为空字符串
        assertFalse(token.isEmpty(), "Token 不应为空字符串");
        
        // 打印 Token 前20位（保护敏感信息）
        System.out.println("获取到的 Token: " + token.substring(0, Math.min(20, token.length())) + "...");
    }

    /**
     * 测试 Token Header 生成功能
     * 验证 Authorization Header 的格式符合 OAuth2.0 规范
     */
    @Test
    @Story("Token Header生成")
    @Description("验证生成的Authorization Header格式正确")
    public void testAuthHeader() {
        // 获取认证 Header 的 Key 和 Value
        String headerKey = TokenUtils.getAuthHeaderKey();
        String headerValue = TokenUtils.getAuthHeaderValue();

        // 验证 Header Key 为 "Authorization"
        assertEquals("Authorization", headerKey, "Header Key 应该是 Authorization");
        // 验证 Header Value 以 "Bearer " 前缀开头
        assertTrue(headerValue.startsWith("Bearer "), "Header Value 应该以 Bearer 开头");
        
        // 打印完整的 Header 信息
        System.out.println("Header: " + headerKey + ": " + headerValue);
    }

    /**
     * 测试 Token 强制刷新功能
     * 验证调用 forceRefreshToken() 后能获取新的 Token
     */
    @Test
    @Story("Token强制刷新")
    @Description("验证可以强制刷新Token")
    public void testForceRefreshToken() {
        // 获取刷新前的 Token
        String oldToken = TokenUtils.getToken();

        // 强制刷新 Token（清除缓存并重新获取）
        TokenUtils.forceRefreshToken();
        
        // 获取刷新后的 Token
        String newToken = TokenUtils.getToken();

        // 断言刷新后的 Token 不为 null
        assertNotNull(newToken, "刷新后的 Token 不应为 null");
        
        // 打印新旧 Token（保护敏感信息）
        System.out.println("旧 Token: " + oldToken.substring(0, Math.min(20, oldToken.length())) + "...");
        System.out.println("新 Token: " + newToken.substring(0, Math.min(20, newToken.length())) + "...");
    }

    /**
     * 测试 Token 清除功能
     * 验证调用 clearToken() 后重新获取会产生新的 Token
     */
    @Test
    @Story("Token清除功能")
    @Description("验证可以清除Token")
    public void testClearToken() {
        // 清除当前 Token 缓存
        TokenUtils.clearToken();

        // 重新获取 Token（应该会触发新的获取流程）
        String token = TokenUtils.getToken();
        
        // 断言重新获取的 Token 不为 null
        assertNotNull(token, "清除后重新获取的 Token 不应为 null");
        
        // 打印重新获取的 Token
        System.out.println("清除后重新获取的 Token: " + token.substring(0, Math.min(20, token.length())) + "...");
    }

    /**
     * 测试根据 ID 获取单个帖子接口
     * 使用 RequestWrapper 和 ResponseExtractor 封装进行接口调用和响应提取
     */
    @Test
    @Story("获取单个帖子")
    @Description("验证根据ID获取帖子接口返回200状态码和正确数据")
    public void testGetPostById() {
        // 调用封装的 GET 请求方法，传入路径参数
        Response response = RequestWrapper.get("/posts/{id}", Map.of("id", 1));

        // 使用 ResponseExtractor 提取响应信息并断言
        assertThat(ResponseExtractor.getStatusCode(response), equalTo(200));
        assertThat(ResponseExtractor.extract(response, "id"), equalTo(1));
        assertThat(ResponseExtractor.extract(response, "userId"), equalTo(1));
        assertThat(ResponseExtractor.extract(response, "title"), notNullValue());
    }

    /**
     * 测试创建帖子接口
     * 使用 POJO 对象作为请求体，验证返回状态码和生成的 ID
     */
    @Test
    @Story("创建帖子")
    @Description("验证创建帖子接口返回201状态码和生成的ID")
    public void testCreatePost() {
        // 构建请求体 POJO 对象
        Post post = new Post();
        post.setUserId(1);
        post.setTitle("接口自动化测试");
        post.setBody("这是POJO模式的请求体");

        // 调用封装的 POST 请求方法
        Response response = RequestWrapper.post("/posts", post);

        // 断言响应状态码为 201（Created）
        assertThat(ResponseExtractor.getStatusCode(response), equalTo(201));
        // 断言返回的 ID 不为 null
        assertThat(ResponseExtractor.extract(response, "id"), notNullValue());
        // 断言返回的 ID 大于 100（jsonplaceholder 模拟数据从 101 开始）
        assertThat(ResponseExtractor.extract(response, "id"), greaterThan(100));
    }

    /**
     * Excel 数据驱动测试的数据提供者
     * 从 Excel 文件中读取测试用例数据
     * 
     * @return 二维数组，每个元素是一个测试用例（Map 格式）
     */
    @DataProvider(name = "excelCases")
    public Object[][] getExcelData() {
        // 从 resources/testdata/postTestCases.xlsx 读取测试数据
        List<Map<String, String>> dataList = ExcelDataProvider.readTestData("testdata/postTestCases.xlsx");
        
        // 转换为 TestNG DataProvider 要求的二维数组格式
        Object[][] data = new Object[dataList.size()][];
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = new Object[]{dataList.get(i)};
        }
        return data;
    }

    /**
     * Excel 数据驱动测试方法
     * 根据 Excel 中配置的方法和参数执行接口测试
     * 
     * @param map 包含 url、method、requestBody、expectedStatus 等字段
     */
    @Test(dataProvider = "excelCases")
    public void testApi(Map<String, String> map) {
        // 从 Map 中提取测试参数
        String url = map.get("url");
        String method = map.get("method");
        String body = map.get("requestBody");
        int code = Integer.parseInt(map.get("expectedStatus"));

        // 根据 HTTP 方法执行相应的请求
        if (method.equalsIgnoreCase("GET")) {
            given().when().get(url).then().statusCode(code);
        } else {
            given().body(body).when().post(url).then().statusCode(code);
        }
    }

    /**
     * 测试数据库连接是否正常
     * 验证 DBUtils 能够正确执行 SQL 查询
     */
    @Test
    @Story("测试数据库连接是否正常")
    @Description("纯数据库查询，验证数据库连接和查询功能")
    public void testDatabaseConnectionOnly() {
        // 定义 SQL 查询语句（使用参数化防止 SQL 注入）
        String sql = "SELECT * FROM posts WHERE id = ?";
        
        // 执行查询并获取单条结果
        Map<String, Object> result = DBUtils.executeQuerySingle(sql, 1);

        // 打印查询结果
        System.out.println("==== 数据库查询成功 ====");
        System.out.println("id: " + result.get("id"));
        System.out.println("title: " + result.get("title"));

        // 断言查询结果不为 null
        assertThat(result, notNullValue());
        // 断言查询的 ID 正确
        assertThat(result.get("id"), equalTo(1));
    }

    /**
     * 帖子完整业务流程测试
     * 测试完整的 CRUD 流程：新增 → 查询 → 编辑 → 删除
     * 使用 jsonblob.com 提供的临时 JSON 存储服务进行测试
     */
    @Test
    @Story("帖子完整业务流程：新增→查询→编辑→删除")
    @Description("接口全链路自动化流程测试，验证完整业务场景")
    public void testPostFullFlow() {
        // ========== 步骤1：新增帖子 ==========
        Post post = new Post();
        post.setUserId(1);
        post.setTitle("流程测试帖子");
        post.setBody("新增流程测试内容");
        
        // 调用 POST 请求创建帖子
        Response createResp = RequestWrapper.post("https://jsonblob.com/api/jsonBlob", post);
        AssertUtils.assertStatusCode(createResp, 201);

        // 关键：jsonblob 的 ID 从响应头 X-jsonblob-id 获取，不是从响应体获取
        String postId = createResp.getHeader("X-jsonblob-id");

        // ========== 步骤2：根据ID查询帖子 ==========
        Response getResp = RequestWrapper.get("https://jsonblob.com/api/jsonBlob/{id}", Map.of("id", postId));
        AssertUtils.assertStatusCode(getResp, 200);
        AssertUtils.assertJsonFieldEqual(getResp, "title", "流程测试帖子");

        // ========== 步骤3：编辑帖子 ==========
        Post updatePost = new Post();
        updatePost.setTitle("修改后的帖子标题");
        updatePost.setBody("修改后的内容");
        
        // 调用 PUT 请求更新帖子（必须传路径参数）
        Response updateResp = RequestWrapper.put("https://jsonblob.com/api/jsonBlob/{id}", updatePost, Map.of("id", postId));
        AssertUtils.assertStatusCode(updateResp, 200);

        // ========== 步骤4：删除帖子 ==========
        Response delResp = RequestWrapper.delete("https://jsonblob.com/api/jsonBlob/{id}", Map.of("id", postId));
        AssertUtils.assertStatusCode(delResp, 204);
    }
}
