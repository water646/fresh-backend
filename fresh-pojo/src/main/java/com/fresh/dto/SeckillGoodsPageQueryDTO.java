package com.fresh.dto;

import lombok.Data;

@Data
public class SeckillGoodsPageQueryDTO {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 状态 0：禁用 1：启用
     */
    private Integer status;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页显示数量
     */
    private int pageSize;
}
