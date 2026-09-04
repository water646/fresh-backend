package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 修改商品时传递的数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "GoodsUpdateDTO", description = "修改商品时传递的数据")
public class GoodsUpdateDTO extends GoodsAddDTO implements Serializable {

    /**
     * 商品id
     */
    @ApiModelProperty(value = "商品id", required = true)
    private Long id;
}
