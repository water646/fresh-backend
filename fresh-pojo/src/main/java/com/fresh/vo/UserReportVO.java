package com.fresh.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户统计返回数据（列表均按天逗号拼接，与前端 ECharts 对齐）
 */
@Data
@Builder
public class UserReportVO implements Serializable {

    /**
     * 日期列表，以逗号分隔
     */
    private String dateList;

    /**
     * 每日用户总量列表（截止当天的累计用户数）
     */
    private String totalUserList;

    /**
     * 每日新增用户列表
     */
    private String newUserList;
}
