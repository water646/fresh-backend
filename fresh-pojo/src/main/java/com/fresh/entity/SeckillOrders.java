package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单
 */
@Data
@TableName("seckill_orders")
public class SeckillOrders {

    /* ---------------- 订单状态常量（status 字段取值） ---------------- */

    /** 待付款 */
    public static final Integer PENDING_PAYMENT = 1;
    /** 待接单 */
    public static final Integer TO_BE_CONFIRMED = 2;
    /** 已接单 */
    public static final Integer CONFIRMED = 3;
    /** 派送中 */
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    /** 已完成 */
    public static final Integer COMPLETED = 5;
    /** 已取消 */
    public static final Integer CANCELLED = 6;

    /* ---------------- 支付状态常量（payStatus 字段取值） ---------------- */

    /** 未支付 */
    public static final Integer PAY_STATUS_UNPAID = 0;
    /** 已支付 */
    public static final Integer PAY_STATUS_PAID = 1;
    /** 退款 */
    public static final Integer PAY_STATUS_REFUND = 2;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String number;

    /**
     * 秒杀商品ID
     */
    private Long seckillGoodsId;

    /**
     * 订单状态
     * 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    private Integer status;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 地址ID
     */
    private Long addressBookId;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 结账时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 支付方式
     * 1微信 2支付宝
     */
    private Integer payMethod;

    /**
     * 支付状态
     * 0未支付 1已支付 2退款
     */
    private Integer payStatus;

    /**
     * 实收金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地址
     */
    private String address;

    /**
     * 收货人
     */
    private String consignee;

    /**
     * 订单取消原因
     */
    private String cancelReason;

    /**
     * 订单拒绝原因
     */
    private String rejectionReason;

    /**
     * 订单取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 配送状态
     * 1立即送出 0选择具体时间
     */
    private Integer deliveryStatus;

    /**
     * 送达时间
     */
    private LocalDateTime deliveryTime;
}