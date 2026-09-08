package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 修改商品时传递的数据
 * 部分更新语义：除 id 外字段均可选，只更新传了的字段（MP 的 updateById 忽略 null）；
 * 起售/停售接口也复用本 DTO（只传 id + status），
 * 因此不继承 GoodsAddDTO，避免带上"名称/价格必填"等新增时的校验
 */
@Data
@Schema(name = "GoodsUpdateDTO", description = "修改商品时传递的数据（部分更新，起售/停售复用）")
public class GoodsUpdateDTO implements Serializable {

    /**
     * 商品id
     */
    @NotNull(message = "商品id不能为空")
    @Schema(description = "商品id", required = true)
    private Long id;

    /**
     * 商品名称
     */
    @Size(max = 50, message = "商品名称长度不能超过50")
    @Schema(description = "商品名称")
    private String name;

    /**
     * 分类id
     */
    @Min(value = 1, message = "分类id不合法")
    @Schema(description = "分类id")
    private Long categoryId;

    /**
     * 价格（单位：元）
     */
    @DecimalMin(value = "0.01", message = "价格不能低于0.01元")
    @Digits(integer = 8, fraction = 2, message = "价格整数部分最多8位、小数部分最多2位")
    @Schema(description = "价格（单位：元）")
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
    @Min(value = 0, message = "库存模式只能为0或1")
    @Max(value = 1, message = "库存模式只能为0或1")
    @Schema(description = "库存模式")
    private Integer stockMode;
}
