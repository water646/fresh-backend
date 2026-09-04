package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户端登录时传递的数据
 */
@Data
@ApiModel(value = "UserLoginDTO", description = "用户端登录时传递的数据")
public class UserLoginDTO implements Serializable {

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    /**
     * 验证码（登录时必填，发送验证码接口不用传）
     */
    @ApiModelProperty(value = "验证码", required = true)
    private String code;
}
