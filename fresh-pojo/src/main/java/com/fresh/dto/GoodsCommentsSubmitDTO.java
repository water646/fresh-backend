package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户提交商品评价时传递的数据
 */
@Data
@Schema(name = "GoodsCommentsSubmitDTO", description = "用户提交商品评价时传递的数据")
public class GoodsCommentsSubmitDTO implements Serializable {

    /**
     * 订单明细id（评价入口在"我的订单-已完成"里的明细上）
     */
    @NotNull(message = "订单明细id不能为空")
    @Schema(description = "订单明细id", required = true)
    private Long orderDetailId;

    /**
     * 评分 1-5星
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    @Schema(description = "评分 1-5星", required = true)
    private Integer rating;

    /**
     * 评价内容
     */
    @Size(max = 500, message = "评价内容不能超过500字")
    @Schema(description = "评价内容")
    private String content;

    /**
     * 是否匿名 0否 1是，不传默认不匿名
     */
    @Min(value = 0, message = "anonymous只能是0或1")
    @Max(value = 1, message = "anonymous只能是0或1")
    @Schema(description = "是否匿名 0否 1是，不传默认0")
    private Integer anonymous;
}
