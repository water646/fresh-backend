package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@TableName("order_detail")
@Schema(name = "OrderDetail", description = "订单明细")
public class OrderDetail implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 商品名称（下单时的快照，不随商品改名变化）
     */
    @Schema(description = "商品名称")
    private String name;

    /**
     * 订单id
     */
    @Schema(description = "订单id")
    private Long orderId;

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
}
