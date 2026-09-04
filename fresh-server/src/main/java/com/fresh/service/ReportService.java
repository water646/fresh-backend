package com.fresh.service;

import com.fresh.vo.GoodsSalesVO;
import com.fresh.vo.OrderReportVO;
import com.fresh.vo.TurnoverReportVO;
import com.fresh.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /**
     * 营业额统计（按天统计"已完成"订单的实收金额合计）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 日期列表与每日营业额
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计（每日用户总量 + 每日新增用户）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 日期列表、用户总量列表、新增用户列表
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计（每日订单数、有效订单数及区间汇总、完成率）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 订单统计结果
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名 Top10（时间区间内"已完成"订单中各商品销量合计）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 按销量倒序的前 10 个商品
     */
    List<GoodsSalesVO> getTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据 Excel 报表（近 30 天，含概览、商品销量 Top10、订单明细）
     * @param response 响应对象，用于设置下载头并写出文件流
     */
    void exportBusinessData(HttpServletResponse response);
}
