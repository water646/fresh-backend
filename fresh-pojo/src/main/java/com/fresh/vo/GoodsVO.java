package com.fresh.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String name;

    /**
     * 分类id
     */
    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 价格（单位：元）
     */
    @Schema(description = "价格（单位：元）")
    private BigDecimal price;

    /**
     * 图片地址
     */
    @Schema(description = "图片地址")
    private String image;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态 1:在售 0:下架
     */
    @Schema(description = "状态 1:在售 0:下架")
    private Integer status;

    /**
     * 库存
     */
    @Schema(description = "库存")
    private Integer stock;

}
