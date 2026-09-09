package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户端分页查询商品评价时传递的数据（商品详情页评价列表）
 */
@Data
@Schema(name = "GoodsCommentsPageQueryDTO", description = "用户端分页查询商品评价时传递的数据")
public class GoodsCommentsPageQueryDTO implements Serializable {

    /**
     * 商品id（评价挂在明细上，查询按商品维度聚合）
     */
    @Schema(description = "商品id", required = true)
    private Long goodsId;

    /**
     * 页码
     */
    @Schema(description = "页码", required = true)
    private int page;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", required = true)
    private int pageSize;
}
