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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具：按商品名关键词查销量，普通订单与秒杀订单两个渠道都统计后合并
 * 口径：已支付且未取消的订单，按支付时间（checkout_time）归属日期
 */
@Component
@Slf4j
public class QueryGoodsSalesTool implements AiTool {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private SeckillOrdersMapper seckillOrdersMapper;

    @Override
    public String name() {
        return "query_goods_sales";
    }

    @Override
    public String description() {
        return "查询某商品在指定日期范围内的销售情况，普通订单和秒杀订单两个渠道都会统计并合并。"
                + "只统计已支付且未取消的订单，按支付时间归属日期，金额单位为元。"
                + "goodsName 支持模糊匹配，如传\"阳光玫瑰\"可匹配到\"阳光玫瑰葡萄\"。"
                + "适合回答\"今天/最近一周卖掉了多少个某商品\"\"某商品卖了多少钱\"这类问题。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("goodsName", Map.of("type", "string", "description", "商品名关键词，模糊匹配"));
        schema.put("beginDate", Map.of("type", "string", "description", "开始日期，格式 yyyy-MM-dd，不传默认今天"));
        schema.put("endDate", Map.of("type", "string", "description", "结束日期，格式 yyyy-MM-dd，不传默认今天"));
        return schema;
    }

    @Override
    public List<String> requiredParams() {
        return List.of("goodsName");
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String goodsName = str(args.get("goodsName"));
        LocalDate begin = date(args.get("beginDate"), LocalDate.now());
        LocalDate end = date(args.get("endDate"), LocalDate.now());
        if (begin.isAfter(end)) {
            throw new IllegalArgumentException("beginDate 不能晚于 endDate");
        }
        if (begin.plusDays(92).isBefore(end)) {
            throw new IllegalArgumentException("日期范围最长 92 天");
        }
        //日期统一折算为 [当天 00:00, 结束日次日 00:00) 的左闭右开区间
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        //普通订单渠道：order_detail.amount 存单价，销售金额 = 单价 × 数量
        for (Map<String, Object> row : ordersMapper.selectGoodsSales(goodsName, beginTime, endTime)) {
            row.put("source", "普通订单");
            items.add(row);
            totalQuantity = totalQuantity.add(toDecimal(row.get("quantity")));
            totalAmount = totalAmount.add(toDecimal(row.get("amount")));
        }
        //秒杀渠道：每单限购 1 件，件数 = 订单数，销售金额 = 实收金额之和
        for (Map<String, Object> row : seckillOrdersMapper.selectSeckillGoodsSales(goodsName, beginTime, endTime)) {
            row.put("source", "秒杀订单");
            items.add(row);
            totalQuantity = totalQuantity.add(toDecimal(row.get("quantity")));
            totalAmount = totalAmount.add(toDecimal(row.get("amount")));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goodsNameLike", goodsName);
        result.put("beginDate", begin.format(DATE));
        result.put("endDate", end.format(DATE));
        result.put("items", items);
        result.put("totalQuantity", totalQuantity);
        result.put("totalAmount", totalAmount);
        return result;
    }

    /**
     * 取必填字符串参数
     */
    private static String str(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new IllegalArgumentException("goodsName 不能为空");
        }
        return String.valueOf(value).trim();
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
