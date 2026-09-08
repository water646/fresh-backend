package com.fresh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "UserLoginVO", description = "用户登录后返回的数据")
public class UserLoginVO implements Serializable {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long id;

    /**
     * jwt令牌
     */
    @Schema(description = "jwt令牌，后续请求放入 authentication 请求头")
    private String token;
}
