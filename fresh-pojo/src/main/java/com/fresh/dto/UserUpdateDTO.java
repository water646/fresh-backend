package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改当前登录用户信息时传递的数据
 * 手机号是登录凭证，不提供修改
 */
@Data
@Schema(name = "UserUpdateDTO", description = "修改当前登录用户信息时传递的数据")
public class UserUpdateDTO implements Serializable {

    /**
     * 姓名
     */
    @Size(max = 50, message = "姓名长度不能超过50")
    @Schema(description = "姓名")
    private String name;

    /**
     * 性别
     */
    @Size(max = 10, message = "性别长度不能超过10")
    @Schema(description = "性别")
    private String sex;

    /**
     * 头像
     */
    @Size(max = 255, message = "头像地址长度不能超过255")
    @Schema(description = "头像")
    private String avatar;

    /**
     * 身份证号（唯一）
     */
    @Pattern(regexp = "^\\d{15}(\\d{2}[0-9Xx])?$", message = "身份证号格式不正确")
    @Schema(description = "身份证号")
    private String idNumber;
}
