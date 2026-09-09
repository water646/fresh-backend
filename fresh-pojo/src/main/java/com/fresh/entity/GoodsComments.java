package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品评价（用户对普通订单中商品的评分与评论）
 */
@Data
@TableName("goods_comments")
@Schema(name = "GoodsComments", description = "商品评价")
public class GoodsComments implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 商品id
     */
    @Schema(description = "商品id")
    private Long goodsId;

    /**
     * 评价用户id
     */
    @Schema(description = "评价用户id")
    private Long userId;

    /**
     * 订单id（校验评价资格用）
     */
    @Schema(description = "订单id")
    private Long orderId;

    /**
     * 订单明细id（一条明细只能评价一次，数据库唯一索引 uk_order_detail 兜底）
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
     * 状态 1显示 0隐藏（管理端下架违规评价）
     */
    @Schema(description = "状态 1显示 0隐藏")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 创建人（用户端为用户id）
     */
    @Schema(description = "创建人")
    private Long createUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updateUser;
}
