package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户（C端）
 */
@Data
@TableName("user")
@ApiModel(value = "User", description = "用户")
public class User implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String name;

    /**
     * 手机号（唯一）
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

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

    /**
     * 状态 1:启用 0:禁用（禁用的用户不允许登录）
     */
    @ApiModelProperty(value = "状态 1:启用 0:禁用")
    private Integer status;

    /**
     * 创建时间（数据库默认 CURRENT_TIMESTAMP，插入时无需设置）
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
