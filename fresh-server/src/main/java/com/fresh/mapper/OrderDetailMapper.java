package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.entity.OrderDetail;
import com.fresh.vo.GoodsSalesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    /**
     * 销量排名 Top10：统计时间区间内"已完成"订单（status=5）中各商品的销量合计
     * @param begin 开始时间（含）
     * @param end 结束时间（含）
     * @return 按销量倒序的前 10 个商品
     */
    @Select("select od.name name, sum(od.number) number from order_detail od, orders o " +
            "where od.order_id = o.id and o.status = 5 and o.order_time between #{begin} and #{end} " +
            "group by od.name order by number desc limit 0,10")
    List<GoodsSalesVO> getTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
