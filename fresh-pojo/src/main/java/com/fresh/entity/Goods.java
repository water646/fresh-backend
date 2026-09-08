package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品
 */
@Data
@TableName("goods")
@Schema(name = "Goods", description = "商品")
public class Goods implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String name;

    /**
     * 分类id
     */
    @Schema(description = "分类id")
    private Long categoryId;

    /**
     * 价格（单位：元）
     */
    @Schema(description = "价格（单位：元）")
    private BigDecimal price;

    /**
     * 图片地址
     */
    @Schema(description = "图片地址")
    private String image;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态 1:在售 0:下架
     */
    @Schema(description = "状态 1:在售 0:下架")
    private Integer status;

    /**
     * 库存
     */
    @Schema(description = "库存")
    private Integer stock;

    /**
     * 库存模式
     */
    @Schema(description = "库存模式")
    private Integer stockMode;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createUser;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updateUser;
}
