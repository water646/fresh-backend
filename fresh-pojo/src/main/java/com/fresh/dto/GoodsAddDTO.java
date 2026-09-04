package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 新增商品时传递的数据
 */
@Data
@ApiModel(value = "GoodsAddDTO", description = "新增商品时传递的数据")
public class GoodsAddDTO implements Serializable {

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称", required = true)
    private String name;

    /**
     * 分类id
     */
    @ApiModelProperty(value = "分类id", required = true)
    private Long categoryId;

    /**
     * 价格（单位：元）
     */
    @ApiModelProperty(value = "价格（单位：元）", required = true)
    private BigDecimal price;

    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址")
    private String image;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 状态 1:在售 0:下架
     */
    @ApiModelProperty(value = "状态 1:在售 0:下架")
    private Integer status;

    /**
     * 库存
     */
    @ApiModelProperty(value = "库存")
    private Integer stock;

    /**
     * 库存模式
     */
    @ApiModelProperty(value = "库存模式")
    private Integer stockMode;
}
