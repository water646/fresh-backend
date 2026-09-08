package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 新增分类时传递的数据
 */
@Data
@Schema(name = "CategoryAddDTO", description = "新增分类时传递的数据")
public class CategoryAddDTO implements Serializable {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    @Schema(description = "分类名称", required = true)
    private String name;

    /**
     * 排序，数字越小越靠前
     */
    @Min(value = 0, message = "排序值不能为负数")
    @Schema(description = "排序，数字越小越靠前")
    private Integer sort;

    /**
     * 状态 1:启用 0:禁用
     */
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    @Schema(description = "状态 1:启用 0:禁用，不传默认为 1")
    private Integer status;
}
