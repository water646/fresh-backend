package com.fresh.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单统计返回数据（列表均按天逗号拼接，与前端 ECharts 对齐）
 */
@Data
@Builder
public class OrderReportVO implements Serializable {

    /**
     * 日期列表，以逗号分隔
     */
    private String dateList;

    /**
     * 每日订单数列表（全部状态）
     */
    private String orderCountList;

    /**
     * 每日有效订单数列表（已完成订单）
     */
    private String validOrderCountList;

    /**
     * 时间区间内订单总数
     */
    private Integer totalOrderCount;

    /**
     * 时间区间内有效订单数（已完成订单）
     */
    private Integer validOrderCount;

    /**
     * 订单完成率（有效订单数 / 订单总数，订单总数为 0 时为 0.0）
     */
    private Double orderCompletionRate;
}
