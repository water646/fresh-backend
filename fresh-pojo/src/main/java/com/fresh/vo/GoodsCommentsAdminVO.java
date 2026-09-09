package com.fresh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端商品评价展示数据（含商品名、用户名等联表信息，不受匿名脱敏影响）
 */
@Data
@Schema(name = "GoodsCommentsAdminVO", description = "管理端商品评价展示数据")
public class GoodsCommentsAdminVO implements Serializable {

    /**
     * 评价id
     */
    @Schema(description = "评价id")
    private Long id;

    /**
     * 商品id
     */
    @Schema(description = "商品id")
    private Long goodsId;

    /**
     * 商品名称（商品被删时为 null）
     */
    @Schema(description = "商品名称")
    private String goodsName;

    /**
     * 评价用户id
     */
    @Schema(description = "评价用户id")
    private Long userId;

    /**
     * 评价用户昵称（管理端可见真实昵称）
     */
    @Schema(description = "评价用户昵称")
    private String userName;

    /**
     * 订单id
     */
    @Schema(description = "订单id")
    private Long orderId;

    /**
     * 订单明细id
     */
    @Schema(description = "订单明细id")
    private Long orderDetailId;

    /**
     * 评分 1-5星
     */
    @Schema(description = "评分 1-5星")
    private Integer rating;

    /**
     * 评价内容
     */
    @Schema(description = "评价内容")
    private String content;

    /**
     * 是否匿名 0否 1是
     */
    @Schema(description = "是否匿名 0否 1是")
    private Integer anonymous;

    /**
     * 商家回复
     */
    @Schema(description = "商家回复")
    private String reply;

    /**
     * 商家回复时间
     */
    @Schema(description = "商家回复时间")
    private LocalDateTime replyTime;

    /**
     * 状态 1显示 0隐藏
     */
    @Schema(description = "状态 1显示 0隐藏")
    private Integer status;

    /**
     * 评价时间
     */
    @Schema(description = "评价时间")
    private LocalDateTime createTime;
}
