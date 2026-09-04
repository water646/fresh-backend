package com.fresh.controller.admin;

import com.fresh.result.Result;
import com.fresh.service.ReportService;
import com.fresh.vo.GoodsSalesVO;
import com.fresh.vo.OrderReportVO;
import com.fresh.vo.TurnoverReportVO;
import com.fresh.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计（管理端报表）
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     * @param begin 开始日期（含），格式 yyyy-MM-dd
     * @param end 结束日期（含），格式 yyyy-MM-dd
     * @return 日期列表与每日营业额
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额统计，区间：{} ~ {}", begin, end);
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /**
     * 用户统计
     * @param begin 开始日期（含），格式 yyyy-MM-dd
     * @param end 结束日期（含），格式 yyyy-MM-dd
     * @return 日期列表、用户总量列表、新增用户列表
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计，区间：{} ~ {}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /**
     * 订单统计
     * @param begin 开始日期（含），格式 yyyy-MM-dd
     * @param end 结束日期（含），格式 yyyy-MM-dd
     * @return 每日订单数/有效订单数及区间汇总、完成率
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("订单统计，区间：{} ~ {}", begin, end);
        return Result.success(reportService.getOrderStatistics(begin, end));
    }

    /**
     * 销量排名 Top10
     * @param begin 开始日期（含），格式 yyyy-MM-dd
     * @param end 结束日期（含），格式 yyyy-MM-dd
     * @return 按销量倒序的前 10 个商品
     */
    @GetMapping("/top10")
    @ApiOperation("销量排名Top10")
    public Result<List<GoodsSalesVO>> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("销量排名Top10，区间：{} ~ {}", begin, end);
        return Result.success(reportService.getTop10(begin, end));
    }

    /**
     * 导出运营数据 Excel 报表（近 30 天，浏览器直接下载 .xls 文件）
     * @param response 响应对象，用于写出文件流
     */
    @GetMapping("/export")
    @ApiOperation("导出运营数据Excel报表")
    public void export(HttpServletResponse response) {
        log.info("导出运营数据Excel报表");
        reportService.exportBusinessData(response);
    }
}
