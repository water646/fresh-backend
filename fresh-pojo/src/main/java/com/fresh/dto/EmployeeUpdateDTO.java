package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改员工时传递的数据
 * 部分更新语义：除 id 外字段均可选，只更新传了的字段；
 * 用户名（登录账号）和密码不通过本接口修改，DTO 中不设这两个字段
 */
@Data
@ApiModel(value = "EmployeeUpdateDTO", description = "修改员工时传递的数据")
public class EmployeeUpdateDTO implements Serializable {

    /**
     * 员工id
     */
    @ApiModelProperty(value = "员工id", required = true)
    private Long id;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String name;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 性别 0女 1男
     */
    @ApiModelProperty(value = "性别 0女 1男")
    private String sex;

    /**
     * 身份证号
     */
    @ApiModelProperty(value = "身份证号")
    private String idNumber;

    /**
     * 状态 0:禁用 1:启用（启用/禁用复用本接口）
     */
    @ApiModelProperty(value = "状态 0:禁用 1:启用")
    private Integer status;
}
