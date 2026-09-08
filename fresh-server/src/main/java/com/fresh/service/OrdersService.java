package com.fresh.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.dto.OrdersPageQueryDTO;
import com.fresh.dto.OrdersSubmitDTO;
import com.fresh.entity.Orders;
import com.fresh.result.PageResult;
import com.fresh.vo.OrderVO;

public interface OrdersService extends IService<Orders> {
    Orders submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付成功：状态 1待付款→2待接单，payStatus 0→1，并推送商家来单提醒。
     * 内部按订单号加 Redisson 锁防重复支付，调用方无需自行加锁
     */
    int paySuccess(String orderNumber);

    /**
     * 查询订单详情（订单基本信息 + 订单明细，仅限当前登录用户自己的订单）
     * @param id 订单id
     * @return 订单详情 VO
     */
    OrderVO getOrderDetail(Long id);

    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);
}
