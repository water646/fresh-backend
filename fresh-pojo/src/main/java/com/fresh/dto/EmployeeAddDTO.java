package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 新增员工时传递的数据
 */
@Data
@Schema(name = "EmployeeAddDTO", description = "新增员工时传递的数据")
public class EmployeeAddDTO implements Serializable {

    /**
     * 姓名
     */
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 32, message = "姓名长度不能超过32")
    @Schema(description = "姓名", required = true)
    private String name;

    /**
     * 用户名（登录账号，唯一）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名长度不能超过32")
    @Schema(description = "用户名", required = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码长度不能超过64")
    @Schema(description = "密码", required = true)
    private String password;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;

    /**
     * 性别 0女 1男
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^[01]$", message = "性别只能为0或1")
    @Schema(description = "性别 0女 1男", required = true)
    private String sex;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{15}(\\d{2}[0-9Xx])?$", message = "身份证号格式不正确")
    @Schema(description = "身份证号", required = true)
    private String idNumber;
}
