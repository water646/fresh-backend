package com.fresh.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 营业额统计返回数据（日期与每日营业额按天逗号拼接，与前端 ECharts 对齐）
 */
@Data
@Builder
public class TurnoverReportVO implements Serializable {

    /**
     * 日期列表，以逗号分隔，如 "2026-08-30,2026-08-31,2026-09-01"
     */
    private String dateList;

    /**
     * 每日营业额列表，以逗号分隔，与 dateList 一一对应，如 "18.7,0.0,36.4"
     */
    private String turnoverList;
}
