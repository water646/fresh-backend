package com.fresh.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.dto.OrdersPageQueryDTO;
import com.fresh.dto.OrdersSubmitDTO;
import com.fresh.entity.Orders;
import com.fresh.result.PageResult;
import com.fresh.vo.OrderVO;

public interface OrdersService extends IService<Orders> {
    Orders submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    int paySuccess(String orderNumber);

    /**
     * 查询订单详情（订单基本信息 + 订单明细，仅限当前登录用户自己的订单）
     * @param id 订单id
     * @return 订单详情 VO
     */
    OrderVO getOrderDetail(Long id);

    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);
}
