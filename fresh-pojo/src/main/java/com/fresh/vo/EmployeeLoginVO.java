package com.fresh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录后返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "EmployeeLoginVO", description = "员工登录后返回的数据")
public class EmployeeLoginVO implements Serializable {

    /**
     * 员工id
     */
    @ApiModelProperty(value = "员工id")
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String name;

    /**
     * jwt令牌
     */
    @ApiModelProperty(value = "jwt令牌")
    private String token;
}
