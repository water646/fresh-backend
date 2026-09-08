package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 员工登录时传递的数据
 */
@Data
@Schema(name = "EmployeeLoginDTO", description = "员工登录时传递的数据")
public class EmployeeLoginDTO implements Serializable {

    /**
     * 用户名
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
}
