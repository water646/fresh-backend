package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增分类时传递的数据
 */
@Data
@ApiModel(value = "CategoryAddDTO", description = "新增分类时传递的数据")
public class CategoryAddDTO implements Serializable {

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    /**
     * 排序，数字越小越靠前
     */
    @ApiModelProperty(value = "排序，数字越小越靠前")
    private Integer sort;

    /**
     * 状态 1:启用 0:禁用
     */
    @ApiModelProperty(value = "状态 1:启用 0:禁用，不传默认为 1")
    private Integer status;
}
