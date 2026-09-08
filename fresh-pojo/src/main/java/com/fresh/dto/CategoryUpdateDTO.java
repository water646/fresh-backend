package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改分类时传递的数据
 * 部分更新语义：除 id 外字段均可选，只更新传了的字段（MP 的 updateById 忽略 null）；
 * 因此不继承 CategoryAddDTO，避免带上"名称必填"等新增时的校验
 */
@Data
@Schema(name = "CategoryUpdateDTO", description = "修改分类时传递的数据")
public class CategoryUpdateDTO implements Serializable {

    /**
     * 分类id
     */
    @NotNull(message = "分类id不能为空")
    @Schema(description = "分类id", required = true)
    private Long id;

    /**
     * 分类名称
     */
    @Size(max = 50, message = "分类名称长度不能超过50")
    @Schema(description = "分类名称")
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
    @Schema(description = "状态 1:启用 0:禁用")
    private Integer status;
}
