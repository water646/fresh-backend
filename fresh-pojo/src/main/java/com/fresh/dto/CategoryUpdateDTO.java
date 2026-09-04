package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 修改分类时传递的数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CategoryUpdateDTO", description = "修改分类时传递的数据")
public class CategoryUpdateDTO extends CategoryAddDTO implements Serializable {

    /**
     * 分类id
     */
    @ApiModelProperty(value = "分类id", required = true)
    private Long id;
}
