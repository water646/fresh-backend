package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.entity.SeckillOrders;
import com.fresh.vo.SeckillOrdersVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 秒杀订单 Mapper：继承 MyBatis-Plus 的 BaseMapper，自带单表 CRUD
 */
@Mapper
public interface SeckillOrdersMapper extends BaseMapper<SeckillOrders> {

    /**
     * 联表分页查询秒杀订单（LEFT JOIN seckill_goods 带出秒杀商品名称，商品被删时 seckillGoodsName 为 null）
     * 配合 PageHelper.startPage 使用，返回的 List 实际是 Page 对象；
     * 条件全部可选：number 模糊、status 精确、phone 模糊、下单时间范围（含两端）；
     * userId 传 null 查全部（管理端），传具体值查该用户自己的订单（用户端）
     */
    @Select("<script>" +
            "SELECT o.*, g.name AS seckillGoodsName FROM seckill_orders o " +
            "LEFT JOIN seckill_goods g ON o.seckill_goods_id = g.id " +
            "<where>" +
            "<if test='number != null and number != \"\"'>AND o.number LIKE CONCAT('%', #{number}, '%') </if>" +
            "<if test='status != null'>AND o.status = #{status} </if>" +
            "<if test='phone != null and phone != \"\"'>AND o.phone LIKE CONCAT('%', #{phone}, '%') </if>" +
            "<if test='beginTime != null'>AND o.order_time &gt;= #{beginTime} </if>" +
            "<if test='endTime != null'>AND o.order_time &lt;= #{endTime} </if>" +
            "<if test='userId != null'>AND o.user_id = #{userId} </if>" +
            "</where>" +
            " ORDER BY o.id DESC" +
            "</script>")
    List<SeckillOrdersVO> listWithGoodsName(@Param("number") String number,
                                            @Param("status") Integer status,
                                            @Param("phone") String phone,
                                            @Param("beginTime") LocalDateTime beginTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("userId") Long userId);

    /**
     * AI 工具查询：按商品名关键词统计秒杀渠道的商品销量
     * 口径：已支付且未取消，按支付时间归属日期；秒杀每单限购 1 件，件数 = 订单数，金额 = 实收金额之和
     * 按秒杀商品名分组返回 goodsName/quantity/amount，时间区间为左闭右开 [beginTime, endTime)
     */
    @Select("SELECT g.name AS goodsName, COUNT(*) AS quantity, SUM(o.amount) AS amount " +
            "FROM seckill_orders o JOIN seckill_goods g ON o.seckill_goods_id = g.id " +
            "WHERE o.pay_status = 1 AND o.status <> 6 " +
            "AND o.checkout_time >= #{beginTime} AND o.checkout_time < #{endTime} " +
            "AND g.name LIKE CONCAT('%', #{name}, '%') " +
            "GROUP BY g.name ORDER BY quantity DESC")
    List<Map<String, Object>> selectSeckillGoodsSales(@Param("name") String name,
                                                      @Param("beginTime") LocalDateTime beginTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * AI 工具查询：秒杀订单渠道的订单统计（单行聚合）
     * 口径：按下单时间归属日期；有效订单 = 已支付且未取消；营业额 = 有效订单实收金额之和
     */
    @Select("SELECT COUNT(*) AS totalOrders, " +
            "SUM(CASE WHEN pay_status = 1 AND status <> 6 THEN 1 ELSE 0 END) AS validOrders, " +
            "SUM(CASE WHEN status = 6 THEN 1 ELSE 0 END) AS cancelledOrders, " +
            "SUM(CASE WHEN pay_status = 1 AND status <> 6 THEN amount ELSE 0 END) AS turnover " +
            "FROM seckill_orders " +
            "WHERE order_time >= #{beginTime} AND order_time < #{endTime}")
    Map<String, Object> selectSeckillOrderStats(@Param("beginTime") LocalDateTime beginTime,
                                                @Param("endTime") LocalDateTime endTime);
}
