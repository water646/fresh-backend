package com.fresh.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品销量统计（销量排名 Top10）
 */
@Data
public class GoodsSalesVO implements Serializable {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 销量（区间内已完成订单中该商品的数量合计）
     */
    private Integer number;
}
