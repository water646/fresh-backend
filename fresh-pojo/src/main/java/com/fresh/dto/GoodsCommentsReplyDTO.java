package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理端回复商品评价时传递的数据
 */
@Data
@Schema(name = "GoodsCommentsReplyDTO", description = "管理端回复商品评价时传递的数据")
public class GoodsCommentsReplyDTO implements Serializable {

    /**
     * 评价id
     */
    @NotNull(message = "评价id不能为空")
    @Schema(description = "评价id", required = true)
    private Long id;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500字")
    @Schema(description = "回复内容", required = true)
    private String reply;
}
