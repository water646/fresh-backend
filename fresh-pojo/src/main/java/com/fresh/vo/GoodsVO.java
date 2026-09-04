package com.fresh.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO {

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
     * 分类id
     */
    @ApiModelProperty(value = "分类id")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    /**
     * 价格（单位：元）
     */
    @ApiModelProperty(value = "价格（单位：元）")
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

}
