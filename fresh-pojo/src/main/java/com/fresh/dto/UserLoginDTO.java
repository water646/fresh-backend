package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户端登录时传递的数据
 */
@Data
@Schema(name = "UserLoginDTO", description = "用户端登录时传递的数据")
public class UserLoginDTO implements Serializable {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;

    /**
     * 验证码（登录时必填，发送验证码接口不用传，故不加校验注解）
     */
    @Schema(description = "验证码", required = true)
    private String code;
}
