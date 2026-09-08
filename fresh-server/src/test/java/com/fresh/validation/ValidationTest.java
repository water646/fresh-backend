package com.fresh.validation;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Controller 参数校验链路的集成测试：DTO 注解 → @Valid → GlobalExceptionHandler 统一格式返回。
 * 用例全部走免登录的放行路径，不依赖业务数据，只依赖中间件可用。
 *
 * 背景：项目曾只有 validation-api 而没有 hibernate-validator 实现，@Valid 静默跳过、
 * 所有校验注解不生效（2026-09-08 补 spring-boot-starter-validation 修复）。
 * 本测试保证该依赖被误删、@Valid 被挪位或 handler 被删时能立刻发现。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 发一个 JSON POST 请求，返回原始响应
     */
    private ResponseEntity<String> post(String uri, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(uri, new HttpEntity<>(json, headers), String.class);
    }

    /**
     * 断言校验被触发且走统一返回格式：HTTP 200 + code=0 + 注解里的中文提示
     */
    private void assertMsg(ResponseEntity<String> resp, String expectedMsg) {
        assertEquals(200, resp.getStatusCode().value());
        JSONObject body = JSONObject.parseObject(resp.getBody());
        assertEquals(0, body.getIntValue("code"));
        assertEquals(expectedMsg, body.getString("msg"));
    }

    /**
     * 用例1：空用户名登录 → 被 @NotBlank 拦截，不会进 Service（否则会返回"账号不存在"）
     */
    @Test
    public void 员工登录_空用户名_应被校验拦截() {
        assertMsg(post("/admin/employee/login", "{\"username\":\"\",\"password\":\"123456\"}"),
                "用户名不能为空");
    }

    /**
     * 用例2：完全不传 username → 同样被 @NotBlank 拦截（注解对 null 也违规）
     */
    @Test
    public void 员工登录_缺用户名_应被校验拦截() {
        assertMsg(post("/admin/employee/login", "{\"password\":\"123456\"}"),
                "用户名不能为空");
    }

    /**
     * 用例3：用户登录缺手机号 → 被 @NotBlank 拦截
     */
    @Test
    public void 用户登录_缺手机号_应被校验拦截() {
        assertMsg(post("/user/user/login", "{\"code\":\"123456\"}"),
                "手机号不能为空");
    }

    /**
     * 用例4：发送验证码传格式非法的手机号 → 被 @Pattern 拦截
     */
    @Test
    public void 发送验证码_手机号格式错误_应被校验拦截() {
        assertMsg(post("/user/user/sendMsg", "{\"phone\":\"123\"}"),
                "手机号格式不正确");
    }

    /**
     * 用例5：格式合法的请求应穿过校验进入业务层——返回的是业务提示"账号不存在"而非校验提示，
     * 证明校验不会误伤合法请求（与用例1形成对照）
     */
    @Test
    public void 合法格式的登录请求_应穿过校验进入业务层() {
        assertMsg(post("/admin/employee/login", "{\"username\":\"ab\",\"password\":\"123456\"}"),
                "账号不存在");
    }
}
