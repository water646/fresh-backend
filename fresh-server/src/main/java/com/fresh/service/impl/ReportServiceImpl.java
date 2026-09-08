package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.entity.Orders;
import com.fresh.entity.User;
import com.fresh.exception.BaseException;
import com.fresh.mapper.OrderDetailMapper;
import com.fresh.mapper.OrdersMapper;
import com.fresh.mapper.UserMapper;
import com.fresh.service.ReportService;
import com.fresh.vo.GoodsSalesVO;
import com.fresh.vo.OrderReportVO;
import com.fresh.vo.TurnoverReportVO;
import com.fresh.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计口径：营业额/有效订单/销量均只计"已完成"订单（status=5），按订单的下单时间归属到天
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    /**
     * 订单状态常量：已完成
     */
    private static final Integer ORDER_STATUS_COMPLETED = 5;

    /**
     * 导出报表统计的时间跨度（近 30 天，不含今天，今天数据不完整）
     */
    private static final int EXPORT_RECENT_DAYS = 30;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计（按天统计"已完成"订单的实收金额合计，无订单的日期补 0.0）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 日期列表与每日营业额
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        //查询区间内已完成订单（只取需要的两列）
        List<Orders> ordersList = listOrders(begin, end, ORDER_STATUS_COMPLETED);

        //按天累加营业额
        Map<LocalDate, Double> turnoverMap = new HashMap<>();
        for (Orders orders : ordersList) {
            LocalDate date = orders.getOrderTime().toLocalDate();
            double amount = orders.getAmount() == null ? 0.0 : orders.getAmount().doubleValue();
            turnoverMap.merge(date, amount, Double::sum);
        }

        //按日期补全（无订单的日期营业额为 0.0）
        List<String> turnoverList = dateList.stream()
                .map(date -> String.valueOf(turnoverMap.getOrDefault(date, 0.0)))
                .collect(Collectors.toList());

        return TurnoverReportVO.builder()
                .dateList(joinByComma(dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    /**
     * 用户统计（每日用户总量为截止当天的累计数，每日新增为当天注册数）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 日期列表、用户总量列表、新增用户列表
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //区间开始前的存量用户（累计的基数）
        Long baseUserCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .lt(User::getCreateTime, beginTime));

        //区间内注册的用户（只取创建时间列，按天分组计数）
        List<User> userList = userMapper.selectList(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, beginTime, endTime)
                .select(User::getCreateTime));
        Map<LocalDate, Integer> newUserMap = new HashMap<>();
        for (User user : userList) {
            newUserMap.merge(user.getCreateTime().toLocalDate(), 1, Integer::sum);
        }

        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        long total = baseUserCount;
        for (LocalDate date : dateList) {
            int newUser = newUserMap.getOrDefault(date, 0);
            total += newUser;
            totalUserList.add(String.valueOf(total));
            newUserList.add(String.valueOf(newUser));
        }

        return UserReportVO.builder()
                .dateList(joinByComma(dateList))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }

    /**
     * 订单统计（每日订单数/有效订单数 + 区间汇总 + 完成率）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 订单统计结果
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        //查询区间内全部订单（只取状态和下单时间两列）
        List<Orders> ordersList = listOrders(begin, end, null);

        //按天分别统计全部订单数和有效（已完成）订单数
        Map<LocalDate, Integer> orderCountMap = new HashMap<>();
        Map<LocalDate, Integer> validOrderCountMap = new HashMap<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (Orders orders : ordersList) {
            LocalDate date = orders.getOrderTime().toLocalDate();
            orderCountMap.merge(date, 1, Integer::sum);
            totalOrderCount++;
            if (ORDER_STATUS_COMPLETED.equals(orders.getStatus())) {
                validOrderCountMap.merge(date, 1, Integer::sum);
                validOrderCount++;
            }
        }

        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            orderCountList.add(String.valueOf(orderCountMap.getOrDefault(date, 0)));
            validOrderCountList.add(String.valueOf(validOrderCountMap.getOrDefault(date, 0)));
        }

        //订单完成率 = 有效订单数 / 订单总数，总订单为 0 时记 0.0，避免除零
        double completionRate = totalOrderCount == 0 ? 0.0
                : validOrderCount * 1.0 / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(joinByComma(dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(completionRate)
                .build();
    }

    /**
     * 销量排名 Top10（时间区间内"已完成"订单中各商品销量合计）
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @return 按销量倒序的前 10 个商品
     */
    public List<GoodsSalesVO> getTop10(LocalDate begin, LocalDate end) {
        //与其他统计接口保持一致：先校验日期范围，避免空指针
        getDateList(begin, end);
        return orderDetailMapper.getTop10(LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
    }

    /**
     * 导出运营数据 Excel 报表（.xls）
     * 内容：概览数据 + 商品销量排名 Top10 + 订单明细（近 30 天，不含今天）
     * @param response 响应对象，用于设置下载头并写出文件流
     */
    public void exportBusinessData(HttpServletResponse response) {
        //统计区间：往前推 30 天到昨天（今天数据不完整，不计入）
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate begin = end.minusDays(EXPORT_RECENT_DAYS - 1L);
        log.info("导出运营数据报表，统计区间：{} ~ {}", begin, end);

        //1.查询区间内全部订单（导出的明细页需要订单号/收货人等完整字段，不能只查统计用的三列）
        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .between(Orders::getOrderTime, LocalDateTime.of(begin, LocalTime.MIN),
                        LocalDateTime.of(end, LocalTime.MAX)));
        List<Orders> completedList = ordersList.stream()
                .filter(o -> ORDER_STATUS_COMPLETED.equals(o.getStatus()))
                .collect(Collectors.toList());
        double turnover = completedList.stream()
                .map(o -> o.getAmount() == null ? BigDecimal.ZERO : o.getAmount())
                .mapToDouble(BigDecimal::doubleValue).sum();
        int totalOrderCount = ordersList.size();
        int validOrderCount = completedList.size();
        double completionRate = totalOrderCount == 0 ? 0.0 : validOrderCount * 1.0 / totalOrderCount;
        double unitPrice = validOrderCount == 0 ? 0.0 : turnover / validOrderCount;
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Long totalUserCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .le(User::getCreateTime, endTime));
        Long newUserCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, beginTime, endTime));
        List<GoodsSalesVO> top10 = getTop10(begin, end);

        //2.通过 POI 生成 Excel（从空白工作簿创建，无需模板文件）
        try (HSSFWorkbook excel = new HSSFWorkbook()) {
            Sheet sheet = excel.createSheet("运营数据");

            //标题与统计区间
            fillRow(sheet, 0, "运营数据报表", headStyle(excel));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
            fillRow(sheet, 1, "统计时间：" + begin + " ~ " + end, null);

            //概览数据
            fillRow(sheet, 3, "概览数据", headStyle(excel));
            fillRow(sheet, 4, new String[]{"营业额", "用户总数", "新增用户", "订单总数", "有效订单数", "订单完成率", "平均客单价"});
            fillRow(sheet, 5, new Object[]{turnover, totalUserCount, newUserCount, totalOrderCount,
                    validOrderCount, completionRate, unitPrice});

            //商品销量排名 Top10
            int rowIdx = 7;
            fillRow(sheet, rowIdx++, "商品销量排名 Top10", headStyle(excel));
            fillRow(sheet, rowIdx++, new String[]{"商品名称", "销量"});
            for (GoodsSalesVO goodsSalesVO : top10) {
                fillRow(sheet, rowIdx++, new Object[]{goodsSalesVO.getName(), goodsSalesVO.getNumber()});
            }

            //订单明细（按下单时间倒序）
            rowIdx++;
            fillRow(sheet, rowIdx++, "订单明细", headStyle(excel));
            fillRow(sheet, rowIdx++, new String[]{"订单号", "订单状态", "下单时间", "收货人", "手机号", "地址", "金额"});
            ordersList.sort((a, b) -> b.getOrderTime().compareTo(a.getOrderTime()));
            for (Orders orders : ordersList) {
                fillRow(sheet, rowIdx++, new Object[]{
                        orders.getNumber(),
                        getStatusName(orders.getStatus()),
                        orders.getOrderTime() == null ? "" : TIME_FORMATTER.format(orders.getOrderTime()),
                        orders.getConsignee(),
                        orders.getPhone(),
                        orders.getAddress(),
                        orders.getAmount() == null ? 0.0 : orders.getAmount().doubleValue()});
            }

            //3.设置下载响应头并写出文件流
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("运营数据报表.xls", "UTF-8"));
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.flush();
        } catch (IOException e) {
            throw new BaseException("导出运营数据报表失败");
        }
    }

    /**
     * 构造从 begin 到 end（含两端）的日期列表
     * @param begin 开始日期
     * @param end 结束日期
     * @return 连续的日期列表
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //开始晚于结束时直接报错，避免下面的循环无法终止
        if (begin == null || end == null || begin.isAfter(end)) {
            throw new BaseException("日期范围不合法");
        }
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 查询时间区间内（按下单时间）的订单
     * @param begin 开始日期（含）
     * @param end 结束日期（含）
     * @param status 订单状态，null 表示全部状态
     * @return 订单列表（只查 amount/status/orderTime 需要的列）
     */
    private List<Orders> listOrders(LocalDate begin, LocalDate end, Integer status) {
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<Orders>()
                .between(Orders::getOrderTime, LocalDateTime.of(begin, LocalTime.MIN),
                        LocalDateTime.of(end, LocalTime.MAX))
                //只取统计需要的列，减少内存与传输开销
                .select(Orders::getAmount, Orders::getStatus, Orders::getOrderTime);
        if (status != null) {
            qw.eq(Orders::getStatus, status);
        }
        return ordersMapper.selectList(qw);
    }

    /**
     * 日期列表转逗号拼接字符串（LocalDate 的 toString 即 yyyy-MM-dd）
     */
    private String joinByComma(List<LocalDate> dateList) {
        return dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
    }

    /**
     * 向指定行的第 0 列写一个值（整行标题场景）
     */
    private void fillRow(Sheet sheet, int rowIndex, Object value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(String.valueOf(value));
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /**
     * 向指定行从头写一组值
     */
    private void fillRow(Sheet sheet, int rowIndex, Object[] values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object value = values[i];
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value == null ? "" : String.valueOf(value));
            }
        }
    }

    /**
     * 标题单元格样式（加粗 + 稍大字号）
     */
    private CellStyle headStyle(HSSFWorkbook excel) {
        CellStyle style = excel.createCellStyle();
        Font font = excel.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    /**
     * 订单状态码转中文名（导出订单明细用）
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 1: return "待付款";
            case 2: return "待接单";
            case 3: return "已接单";
            case 4: return "派送中";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return String.valueOf(status);
        }
    }
}
