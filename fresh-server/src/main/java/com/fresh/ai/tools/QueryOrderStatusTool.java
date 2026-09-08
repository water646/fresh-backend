package com.fresh.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.ai.AiTool;
import com.fresh.entity.Orders;
import com.fresh.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工具：多条件组合查询订单明细列表（orders 表，普通订单，不含秒杀订单）
 * 过滤维度：订单号/用户名/手机号/收货人/地址关键词、订单状态、支付状态、支付方式、
 * 下单日期范围、实收金额区间；支持按下单时间或金额排序
 */
@Component
@Slf4j
public class QueryOrderStatusTool implements AiTool {

    /** sortBy / sortOrder 允许的取值（白名单） */
    private static final Set<String> SORT_BY = Set.of("order_time", "amount");
    private static final Set<String> SORT_ORDER = Set.of("asc", "desc");
    /** 与前端展示口径一致的时间格式（无秒） */
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public String name() {
        return "query_order_status";
    }

    @Override
    public String description() {
        return "查询订单明细列表（普通订单，不含秒杀订单），支持多条件组合过滤：订单号/用户名/手机号/收货人/地址关键词、"
                + "订单状态、支付状态、支付方式、下单日期范围、实收金额区间，并可按下单时间或金额排序。"
                + "例如：\"今天待接单的订单\"传 status=2 加当天日期；\"张三的订单\"传 userName=张三；"
                + "\"实收金额超过200元的已完成订单\"传 status=5、minAmount=200；\"被取消的订单和原因\"传 status=6。"
                + "枚举——status：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消；"
                + "payStatus：0未支付 1已支付 2退款；payMethod：1微信 2支付宝。"
                + "日期参数按下单时间（orderTime）过滤，左闭右闭。"
                + "返回字段：id、number（订单号）、status、payStatus、payMethod、userId、userName、phone、"
                + "consignee（收货人）、address、orderTime（下单时间）、checkoutTime（结账时间）、"
                + "amount（实收金额，元）、remark、cancelReason（取消原因）、rejectionReason（拒单原因）、cancelTime（取消时间）。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("number", Map.of("type", "string", "description", "订单号关键词，模糊匹配，可不传"));
        schema.put("status", Map.of("type", "integer", "description", "订单状态，可不传查全部",
                "enum", List.of(1, 2, 3, 4, 5, 6)));
        schema.put("payStatus", Map.of("type", "integer", "description", "支付状态，可不传查全部",
                "enum", List.of(0, 1, 2)));
        schema.put("payMethod", Map.of("type", "integer", "description", "支付方式，可不传查全部",
                "enum", List.of(1, 2)));
        schema.put("userId", Map.of("type", "integer", "description", "下单用户 id，可不传"));
        schema.put("userName", Map.of("type", "string", "description", "用户名关键词，模糊匹配，可不传"));
        schema.put("phone", Map.of("type", "string", "description", "手机号关键词，模糊匹配，可不传"));
        schema.put("consignee", Map.of("type", "string", "description", "收货人姓名关键词，模糊匹配，可不传"));
        schema.put("address", Map.of("type", "string", "description", "收货地址关键词，模糊匹配，可不传"));
        schema.put("beginDate", Map.of("type", "string", "description", "下单日期下限，格式 yyyy-MM-dd，可不传"));
        schema.put("endDate", Map.of("type", "string", "description", "下单日期上限，格式 yyyy-MM-dd（含当天），可不传"));
        schema.put("minAmount", Map.of("type", "number", "description", "最低实收金额（元），可不传"));
        schema.put("maxAmount", Map.of("type", "number", "description", "最高实收金额（元），可不传"));
        schema.put("sortBy", Map.of("type", "string", "description", "排序字段 order_time 或 amount，可不传（默认下单时间倒序）",
                "enum", List.of("order_time", "amount")));
        schema.put("sortOrder", Map.of("type", "string", "description", "排序方向 asc 或 desc，可不传（默认 desc）",
                "enum", List.of("asc", "desc")));
        schema.put("limit", Map.of("type", "integer", "description", "返回条数，默认 20，最大 50"));
        return schema;
    }

    @Override
    public List<String> requiredParams() {
        return List.of();
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String number = text(args.get("number"));
        Integer status = integer(args.get("status"), "status");
        Integer payStatus = integer(args.get("payStatus"), "payStatus");
        Integer payMethod = integer(args.get("payMethod"), "payMethod");
        Integer userId = integer(args.get("userId"), "userId");
        String userName = text(args.get("userName"));
        String phone = text(args.get("phone"));
        String consignee = text(args.get("consignee"));
        String address = text(args.get("address"));
        LocalDate beginDate = date(args.get("beginDate"), "beginDate");
        LocalDate endDate = date(args.get("endDate"), "endDate");
        BigDecimal minAmount = decimal(args.get("minAmount"), "minAmount");
        BigDecimal maxAmount = decimal(args.get("maxAmount"), "maxAmount");
        String sortBy = text(args.get("sortBy"));
        String sortOrder = text(args.get("sortOrder"));
        int limit = clamp(integer(args.get("limit"), "limit"), 20, 1, 50);

        if (status != null && (status < 1 || status > 6)) {
            throw new IllegalArgumentException("status 应为 1~6：" + status);
        }
        if (payStatus != null && (payStatus < 0 || payStatus > 2)) {
            throw new IllegalArgumentException("payStatus 应为 0~2：" + payStatus);
        }
        if (payMethod != null && (payMethod < 1 || payMethod > 2)) {
            throw new IllegalArgumentException("payMethod 应为 1~2：" + payMethod);
        }
        if (sortBy != null && !SORT_BY.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy 只支持 order_time/amount：" + sortBy);
        }
        if (sortOrder != null && !SORT_ORDER.contains(sortOrder)) {
            throw new IllegalArgumentException("sortOrder 只支持 asc/desc：" + sortOrder);
        }
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount 不能大于 maxAmount");
        }
        if (beginDate != null && endDate != null && beginDate.isAfter(endDate)) {
            throw new IllegalArgumentException("beginDate 不能晚于 endDate");
        }

        //动态过滤（各条件可选，MP 条件构造器按"条件成立才拼接"处理）；日期按下单时间，endDate 含当天（右开到次日零点）
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.like(number != null, Orders::getNumber, number)
                .eq(status != null, Orders::getStatus, status)
                .eq(payStatus != null, Orders::getPayStatus, payStatus)
                .eq(payMethod != null, Orders::getPayMethod, payMethod)
                .eq(userId != null, Orders::getUserId, userId)
                .like(userName != null, Orders::getUserName, userName)
                .like(phone != null, Orders::getPhone, phone)
                .like(consignee != null, Orders::getConsignee, consignee)
                .like(address != null, Orders::getAddress, address)
                .ge(beginDate != null, Orders::getOrderTime, beginDate == null ? null : beginDate.atStartOfDay())
                .lt(endDate != null, Orders::getOrderTime, endDate == null ? null : endDate.plusDays(1).atStartOfDay())
                .ge(minAmount != null, Orders::getAmount, minAmount)
                .le(maxAmount != null, Orders::getAmount, maxAmount);

        //命中总数（未截断），再加排序与条数上限；默认按下单时间倒序（商家最常看最新订单）
        Long matchedCount = ordersMapper.selectCount(qw);
        boolean asc = "asc".equals(sortOrder);
        if ("amount".equals(sortBy)) {
            qw.orderBy(true, asc, Orders::getAmount);
        } else if ("order_time".equals(sortBy)) {
            qw.orderBy(true, asc, Orders::getOrderTime);
        } else {
            qw.orderByDesc(Orders::getOrderTime);
        }
        qw.orderByDesc(Orders::getId); //时间/金额相同时再按 id 倒序，保证结果稳定
        qw.last("LIMIT " + limit); //limit 已收敛为 1~50 的整数，无注入风险

        List<Orders> ordersList = ordersMapper.selectList(qw);

        //返回列白名单：只暴露订单事实字段，createUser/updateUser 等审计字段不出库
        List<Map<String, Object>> items = new ArrayList<>();
        for (Orders order : ordersList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", order.getId());
            item.put("number", order.getNumber());
            item.put("status", order.getStatus());
            item.put("payStatus", order.getPayStatus());
            item.put("payMethod", order.getPayMethod());
            item.put("userId", order.getUserId());
            item.put("userName", order.getUserName());
            item.put("phone", order.getPhone());
            item.put("consignee", order.getConsignee());
            item.put("address", order.getAddress());
            item.put("orderTime", format(order.getOrderTime()));
            item.put("checkoutTime", format(order.getCheckoutTime()));
            item.put("amount", order.getAmount());
            item.put("remark", order.getRemark());
            item.put("cancelReason", order.getCancelReason());
            item.put("rejectionReason", order.getRejectionReason());
            item.put("cancelTime", format(order.getCancelTime()));
            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matchedCount", matchedCount);
        result.put("returnedCount", items.size());
        result.put("items", items);
        return result;
    }

    /**
     * 时间转字符串（与前端展示口径一致），null 保持 null
     */
    private static String format(LocalDateTime time) {
        return time == null ? null : time.format(TIME);
    }

    /**
     * 取可选字符串参数，空白视为未传
     */
    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * 取可选日期参数（下单时间范围用）
     */
    private static LocalDate date(Object value, String name) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(String.valueOf(value).trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(name + " 日期格式应为 yyyy-MM-dd：" + value);
        }
    }

    /**
     * 取可选数字参数（金额区间用）
     */
    private static BigDecimal decimal(Object value, String name) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " 应为数字：" + value);
        }
    }

    /**
     * 取可选整数参数
     */
    private static Integer integer(Object value, String name) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " 应为整数：" + value);
        }
    }

    /**
     * 整数收敛到 [min, max] 区间（缺省取默认值）
     */
    private static int clamp(Integer value, int defaultValue, int min, int max) {
        if (value == null) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, value));
    }

}
