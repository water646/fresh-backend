package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加购物车时传递的数据
 */
@Data
@ApiModel(value = "ShoppingCartAddDTO", description = "添加购物车时传递的数据")
public class ShoppingCartAddDTO implements Serializable {

    /**
     * 商品id
     */
    @ApiModelProperty(value = "商品id", required = true)
    private Long goodsId;

    /**
     * 添加数量，不传默认为 1
     */
    @ApiModelProperty(value = "添加数量，不传默认为 1")
    private Integer number;
}
