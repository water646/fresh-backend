package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理端分页查询商品评价时传递的数据
 */
@Data
@Schema(name = "GoodsCommentsAdminPageQueryDTO", description = "管理端分页查询商品评价时传递的数据")
public class GoodsCommentsAdminPageQueryDTO implements Serializable {

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

    /**
     * 商品id（精确）
     */
    @Schema(description = "商品id")
    private Long goodsId;

    /**
     * 评分（精确，1-5）
     */
    @Schema(description = "评分 1-5")
    private Integer rating;

    /**
     * 状态 1显示 0隐藏（精确）
     */
    @Schema(description = "状态 1显示 0隐藏")
    private Integer status;

    /**
     * 评价内容（模糊）
     */
    @Schema(description = "评价内容关键词")
    private String content;
}
