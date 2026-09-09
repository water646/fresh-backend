package com.fresh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户端商品评价展示数据（商品详情页评价列表项）
 */
@Data
@Schema(name = "GoodsCommentsVO", description = "用户端商品评价展示数据")
public class GoodsCommentsVO implements Serializable {

    /**
     * 评价id
     */
    @Schema(description = "评价id")
    private Long id;

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
     * 是否匿名 0否 1是（匿名评价 userName 显示"匿名用户"）
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
     * 评价时间
     */
    @Schema(description = "评价时间")
    private LocalDateTime createTime;

    /**
     * 评价用户昵称（匿名评价已脱敏为"匿名用户"）
     */
    @Schema(description = "评价用户昵称")
    private String userName;

    /**
     * 评价用户头像（匿名评价不返回）
     */
    @Schema(description = "评价用户头像")
    private String userAvatar;
}
