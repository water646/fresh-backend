package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改当前登录用户信息时传递的数据
 * 手机号是登录凭证，不提供修改
 */
@Data
@ApiModel(value = "UserUpdateDTO", description = "修改当前登录用户信息时传递的数据")
public class UserUpdateDTO implements Serializable {

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String name;

    /**
     * 性别
     */
    @ApiModelProperty(value = "性别")
    private String sex;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像")
    private String avatar;

    /**
     * 身份证号（唯一）
     */
    @ApiModelProperty(value = "身份证号")
    private String idNumber;
}
