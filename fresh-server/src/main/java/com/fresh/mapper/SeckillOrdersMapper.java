package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.entity.SeckillOrders;
import com.fresh.vo.SeckillOrdersVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

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
}
