package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改员工时传递的数据
 * 部分更新语义：除 id 外字段均可选，只更新传了的字段；
 * 用户名（登录账号）和密码不通过本接口修改，DTO 中不设这两个字段
 */
@Data
@Schema(name = "EmployeeUpdateDTO", description = "修改员工时传递的数据")
public class EmployeeUpdateDTO implements Serializable {

    /**
     * 员工id
     */
    @NotNull(message = "员工id不能为空")
    @Schema(description = "员工id", required = true)
    private Long id;

    /**
     * 姓名
     */
    @Size(max = 32, message = "姓名长度不能超过32")
    @Schema(description = "姓名")
    private String name;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别 0女 1男
     */
    @Pattern(regexp = "^[01]$", message = "性别只能为0或1")
    @Schema(description = "性别 0女 1男")
    private String sex;

    /**
     * 身份证号
     */
    @Pattern(regexp = "^\\d{15}(\\d{2}[0-9Xx])?$", message = "身份证号格式不正确")
    @Schema(description = "身份证号")
    private String idNumber;

    /**
     * 状态 0:禁用 1:启用（启用/禁用复用本接口）
     */
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;
}
