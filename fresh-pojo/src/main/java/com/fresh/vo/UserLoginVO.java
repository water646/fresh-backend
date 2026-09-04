package com.fresh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录后返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserLoginVO", description = "用户登录后返回的数据")
public class UserLoginVO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long id;

    /**
     * jwt令牌
     */
    @ApiModelProperty(value = "jwt令牌，后续请求放入 authentication 请求头")
    private String token;
}
