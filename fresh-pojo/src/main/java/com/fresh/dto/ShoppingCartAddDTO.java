package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 添加购物车时传递的数据
 */
@Data
@Schema(name = "ShoppingCartAddDTO", description = "添加购物车时传递的数据")
public class ShoppingCartAddDTO implements Serializable {

    /**
     * 商品id
     */
    @NotNull(message = "商品id不能为空")
    @Schema(description = "商品id", required = true)
    private Long goodsId;

    /**
     * 添加数量，不传默认为 1
     */
    @Min(value = 1, message = "添加数量至少为1")
    @Schema(description = "添加数量，不传默认为 1")
    private Integer number;
}
