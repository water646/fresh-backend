package com.fresh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.dto.SeckillOrdersPageQueryDTO;
import com.fresh.dto.SeckillOrdersPayDTO;
import com.fresh.dto.SeckillOrdersUpdateDTO;
import com.fresh.entity.SeckillOrders;
import com.fresh.result.PageResult;
import com.fresh.vo.SeckillOrdersVO;

public interface SeckillOrdersService extends IService<SeckillOrders> {

    /**
     * 按id查秒杀订单详情（带秒杀商品名称）
     * @param id 秒杀订单id
     * @return 订单 VO（商品被删时 seckillGoodsName 为 null；订单不存在返回 null）
     */
    SeckillOrdersVO getByIdWithGoods(Long id);

    /**
     * 落库秒杀订单并扣减数据库库存（MQ 消费者调用，两步在同一事务：扣库存成功才存订单）
     * @param seckillOrders 秒杀订单
     */
    void createOrder(SeckillOrders seckillOrders);

    /**
     * 分页查询秒杀订单（管理端，联表带出秒杀商品名称）
     * @param seckillOrdersPageQueryDTO 分页查询条件
     * @return 分页结果（records 为 SeckillOrdersVO 列表）
     */
    PageResult pageQuery(SeckillOrdersPageQueryDTO seckillOrdersPageQueryDTO);

    /**
     * 分页查询当前用户自己的秒杀订单（用户端，只能查到自己的）
     * @param seckillOrdersPageQueryDTO 分页查询条件（status 可选，其余条件对用户端无效）
     * @return 分页结果（records 为 SeckillOrdersVO 列表，含秒杀商品名称）
     */
    PageResult pageQueryMine(SeckillOrdersPageQueryDTO seckillOrdersPageQueryDTO);

    /**
     * 秒杀订单支付：用地址簿填充收货信息并完成支付
     * （订单状态 1待付款→2待接单，payStatus 0→1，记录结账时间，快照收货人/手机号/详细地址）
     * @param seckillOrdersPayDTO 订单号 + 地址簿id + 支付方式
     */
    void pay(SeckillOrdersPayDTO seckillOrdersPayDTO);

    /**
     * 修改秒杀订单（部分更新：只改传了的字段）
     * @param seckillOrdersUpdateDTO 修改内容（含订单id）
     */
    void update(SeckillOrdersUpdateDTO seckillOrdersUpdateDTO);
}
