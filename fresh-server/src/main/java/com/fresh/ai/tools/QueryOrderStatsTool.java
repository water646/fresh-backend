package com.fresh.ai.tools;

import com.fresh.ai.AiTool;
import com.fresh.mapper.OrdersMapper;
import com.fresh.mapper.SeckillOrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具：查日期范围内的订单统计（总单量、有效单量、取消单量、营业额），普通订单与秒杀订单分渠道统计后合并
 * 口径：按下单时间（order_time）归属日期；有效订单 = 已支付且未取消；营业额 = 有效订单实收金额之和
 */
@Component
@Slf4j
public class QueryOrderStatsTool implements AiTool {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private SeckillOrdersMapper seckillOrdersMapper;

    @Override
    public String name() {
        return "query_order_stats";
    }

    @Override
    public String description() {
        return "查询指定日期范围内的订单统计：总单量、有效单量、取消单量、营业额，普通订单与秒杀订单分渠道统计后合并。"
                + "口径：按下单时间归属日期；有效订单 = 已支付且未取消；营业额 = 有效订单实收金额之和，单位元。"
                + "适合回答\"今天多少单\"\"今天营业额多少\"\"这个月取消了几单\"这类问题。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("beginDate", Map.of("type", "string", "description", "开始日期，格式 yyyy-MM-dd，不传默认今天"));
        schema.put("endDate", Map.of("type", "string", "description", "结束日期，格式 yyyy-MM-dd，不传默认今天"));
        return schema;
    }

    @Override
    public List<String> requiredParams() {
        return List.of();
    }

    @Override
    public Object execute(Map<String, Object> args) {
        LocalDate begin = date(args.get("beginDate"), LocalDate.now());
        LocalDate end = date(args.get("endDate"), LocalDate.now());
        if (begin.isAfter(end)) {
            throw new IllegalArgumentException("beginDate 不能晚于 endDate");
        }
        if (begin.plusDays(92).isBefore(end)) {
            throw new IllegalArgumentException("日期范围最长 92 天");
        }
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        Map<String, Object> normal = channel(ordersMapper.selectOrderStats(beginTime, endTime));
        Map<String, Object> seckill = channel(seckillOrdersMapper.selectSeckillOrderStats(beginTime, endTime));

        //两渠道合计
        Map<String, Object> total = new LinkedHashMap<>();
        total.put("totalOrders", toLong(normal.get("totalOrders")) + toLong(seckill.get("totalOrders")));
        total.put("validOrders", toLong(normal.get("validOrders")) + toLong(seckill.get("validOrders")));
        total.put("cancelledOrders", toLong(normal.get("cancelledOrders")) + toLong(seckill.get("cancelledOrders")));
        total.put("turnover", toDecimal(normal.get("turnover")).add(toDecimal(seckill.get("turnover"))));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("beginDate", begin.format(DATE));
        result.put("endDate", end.format(DATE));
        result.put("normal", normal);
        result.put("seckill", seckill);
        result.put("total", total);
        return result;
    }

    /**
     * 整理单渠道的聚合结果（聚合列为 null 时归 0）
     */
    private static Map<String, Object> channel(Map<String, Object> row) {
        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("totalOrders", toLong(row == null ? null : row.get("totalOrders")));
        channel.put("validOrders", toLong(row == null ? null : row.get("validOrders")));
        channel.put("cancelledOrders", toLong(row == null ? null : row.get("cancelledOrders")));
        channel.put("turnover", toDecimal(row == null ? null : row.get("turnover")));
        return channel;
    }

    /**
     * 取日期参数，空/缺省时返回默认值，格式错误抛出明确提示
     */
    private static LocalDate date(Object value, LocalDate defaultValue) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(String.valueOf(value).trim(), DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd：" + value);
        }
    }

    /**
     * 数值空安全转 long（聚合结果可能为 null）
     */
    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    /**
     * 数值空安全转 BigDecimal（聚合结果可能为 null）
     */
    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

}
