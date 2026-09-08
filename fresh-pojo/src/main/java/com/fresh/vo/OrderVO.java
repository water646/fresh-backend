package com.fresh.vo;

import com.fresh.entity.OrderDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情 VO（订单基本信息 + 订单明细列表），用于向前端展示
 */
@Data
@Schema(name = "OrderVO", description = "订单详情")
public class OrderVO implements Serializable {

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String number;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @Schema(description = "订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消")
    private Integer status;

    /**
     * 下单时间
     */
    @Schema(description = "下单时间")
    private LocalDateTime orderTime;

    /**
     * 结账时间
     */
    @Schema(description = "结账时间")
    private LocalDateTime checkoutTime;

    /**
     * 支付方式 1微信 2支付宝
     */
    @Schema(description = "支付方式 1微信 2支付宝")
    private Integer payMethod;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    @Schema(description = "支付状态 0未支付 1已支付 2退款")
    private Integer payStatus;

    /**
     * 实收金额
     */
    @Schema(description = "实收金额")
    private BigDecimal amount;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 收货人
     */
    @Schema(description = "收货人")
    private String consignee;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 地址
     */
    @Schema(description = "地址")
    private String address;

    /**
     * 订单取消原因
     */
    @Schema(description = "订单取消原因")
    private String cancelReason;

    /**
     * 订单拒绝原因
     */
    @Schema(description = "订单拒绝原因")
    private String rejectionReason;

    /**
     * 订单取消时间
     */
    @Schema(description = "订单取消时间")
    private LocalDateTime cancelTime;

    /**
     * 预计送达时间
     */
    @Schema(description = "预计送达时间")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 配送状态 1立即送出 0选择具体时间
     */
    @Schema(description = "配送状态 1立即送出 0选择具体时间")
    private Integer deliveryStatus;

    /**
     * 送达时间
     */
    @Schema(description = "送达时间")
    private LocalDateTime deliveryTime;

    /**
     * 订单明细列表（下单时的商品快照）
     */
    @Schema(description = "订单明细列表")
    private List<OrderDetail> orderDetailList;

}
