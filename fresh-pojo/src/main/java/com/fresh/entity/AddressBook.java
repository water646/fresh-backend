package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 地址簿（C端用户收货地址）
 * 注意：表内没有 create/update 等审计字段，
 * 后续 Mapper 不要标注 @AutoFill，直接用 BaseMapper 即可
 */
@Data
@TableName("address_book")
@Schema(name = "AddressBook", description = "地址簿")
public class AddressBook implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 收货人
     */
    @Schema(description = "收货人")
    private String consignee;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别 0女 1男
     */
    @Schema(description = "性别 0女 1男")
    private String sex;


    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String detail;

    /**
     * 标签（如"家"、"公司"、"学校"）
     */
    @Schema(description = "标签")
    private String label;

}
