package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 新增商品时传递的数据
 */
@Data
@Schema(name = "GoodsAddDTO", description = "新增商品时传递的数据")
public class GoodsAddDTO implements Serializable {

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名不能为空")
    @Size(max = 50,message = "名称长度超过限定")
    @Schema(description = "商品名称", required = true)
    private String name;

    /**
     * 分类id
     */
    @NotNull(message = "分类id不能为空")
    @Schema(description = "分类id", required = true)
    private Long categoryId;

    /**
     * 价格（单位：元）
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格不能低于0.01元")
    @Digits(integer = 4, fraction = 2, message = "价格整数部分最多4位、小数部分最多2位")
    @Schema(description = "价格（单位：元）", required = true)
    private BigDecimal price;

    /**
     * 图片地址
     */
    @Size(max = 200, message = "图片地址长度不能超过200")
    @Schema(description = "图片地址")
    private String image;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过500")
    @Schema(description = "描述")
    private String description;

    /**
     * 状态 1:在售 0:下架
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    @Schema(description = "状态 1:在售 0:下架")
    private Integer status;

    /**
     * 库存
     */
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 100000, message = "库存不能超过100000")
    @Schema(description = "库存")
    private Integer stock;

    /**
     * 库存模式
     */
    @NotNull(message = "库存模式不能为空")
    @Min(value = 0, message = "库存模式只能为0或1")
    @Max(value = 1, message = "库存模式只能为0或1")
    @Schema(description = "库存模式")
    private Integer stockMode;
}
