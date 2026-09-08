package com.fresh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户信息
 */
@Data
@Schema(name = "UserVO", description = "当前登录用户信息")
public class UserVO implements Serializable {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long id;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别
     */
    @Schema(description = "性别")
    private String sex;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;
}
