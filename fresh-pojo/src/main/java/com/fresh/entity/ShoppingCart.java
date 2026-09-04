package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@TableName("shopping_cart")
@ApiModel(value = "ShoppingCart", description = "购物车")
public class ShoppingCart implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    private String name;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 商品id
     */
    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    /**
     * 商品数量
     */
    @ApiModelProperty(value = "商品数量")
    private Integer number;

    /**
     * 商品金额
     */
    @ApiModelProperty(value = "商品金额")
    private BigDecimal amount;

    /**
     * 商品图片
     */
    @ApiModelProperty(value = "商品图片")
    private String image;

    /**
     * 创建时间（数据库默认 CURRENT_TIMESTAMP，插入时无需设置）
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
