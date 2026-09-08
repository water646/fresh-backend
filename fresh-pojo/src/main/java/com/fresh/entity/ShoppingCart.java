package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@TableName("shopping_cart")
@Schema(name = "ShoppingCart", description = "购物车")
public class ShoppingCart implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String name;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 商品id
     */
    @Schema(description = "商品id")
    private Long goodsId;

    /**
     * 商品数量
     */
    @Schema(description = "商品数量")
    private Integer number;

    /**
     * 商品金额
     */
    @Schema(description = "商品金额")
    private BigDecimal amount;

    /**
     * 商品图片
     */
    @Schema(description = "商品图片")
    private String image;

    /**
     * 创建时间（数据库默认 CURRENT_TIMESTAMP，插入时无需设置）
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
