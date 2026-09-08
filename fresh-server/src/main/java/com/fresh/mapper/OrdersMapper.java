package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * AI 工具查询：按商品名关键词统计普通订单渠道的商品销量
     * 口径：已支付且未取消，按支付时间归属日期；order_detail.amount 存单价，金额 = 单价 × 数量
     * 按商品名分组返回 goodsName/quantity/amount，时间区间为左闭右开 [beginTime, endTime)
     */
    @Select("SELECT od.name AS goodsName, SUM(od.number) AS quantity, SUM(od.amount * od.number) AS amount " +
            "FROM order_detail od JOIN orders o ON od.order_id = o.id " +
            "WHERE o.pay_status = 1 AND o.status <> 6 " +
            "AND o.checkout_time >= #{beginTime} AND o.checkout_time < #{endTime} " +
            "AND od.name LIKE CONCAT('%', #{name}, '%') " +
            "GROUP BY od.name ORDER BY quantity DESC")
    List<Map<String, Object>> selectGoodsSales(@Param("name") String name,
                                               @Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * AI 工具查询：普通订单渠道的订单统计（单行聚合）
     * 口径：按下单时间归属日期；有效订单 = 已支付且未取消；营业额 = 有效订单实收金额之和
     */
    @Select("SELECT COUNT(*) AS totalOrders, " +
            "SUM(CASE WHEN pay_status = 1 AND status <> 6 THEN 1 ELSE 0 END) AS validOrders, " +
            "SUM(CASE WHEN status = 6 THEN 1 ELSE 0 END) AS cancelledOrders, " +
            "SUM(CASE WHEN pay_status = 1 AND status <> 6 THEN amount ELSE 0 END) AS turnover " +
            "FROM orders " +
            "WHERE order_time >= #{beginTime} AND order_time < #{endTime}")
    Map<String, Object> selectOrderStats(@Param("beginTime") LocalDateTime beginTime,
                                         @Param("endTime") LocalDateTime endTime);

}
