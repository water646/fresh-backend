package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户（C端）
 */
@Data
@TableName("user")
@Schema(name = "User", description = "用户")
public class User implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 手机号（唯一）
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

    /**
     * 身份证号（唯一）
     */
    @Schema(description = "身份证号")
    private String idNumber;

    /**
     * 状态 1:启用 0:禁用（禁用的用户不允许登录）
     */
    @Schema(description = "状态 1:启用 0:禁用")
    private Integer status;

    /**
     * 创建时间（数据库默认 CURRENT_TIMESTAMP，插入时无需设置）
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
