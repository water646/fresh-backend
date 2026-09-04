package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品
 */
@Data
@TableName("goods")
@ApiModel(value = "Goods", description = "商品")
public class Goods implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    private String name;

    /**
     * 分类id
     */
    @ApiModelProperty(value = "分类id")
    private Long categoryId;

    /**
     * 价格（单位：元）
     */
    @ApiModelProperty(value = "价格（单位：元）")
    private BigDecimal price;

    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址")
    private String image;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 状态 1:在售 0:下架
     */
    @ApiModelProperty(value = "状态 1:在售 0:下架")
    private Integer status;

    /**
     * 库存
     */
    @ApiModelProperty(value = "库存")
    private Integer stock;

    /**
     * 库存模式
     */
    @ApiModelProperty(value = "库存模式")
    private Integer stockMode;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private Long createUser;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    @ApiModelProperty(value = "修改人id")
    private Long updateUser;
}
