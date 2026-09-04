package com.fresh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@TableName("orders")
@ApiModel(value = "Orders", description = "订单")
public class Orders implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String number;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @ApiModelProperty(value = "订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消")
    private Integer status;

    /**
     * 下单用户id
     */
    @ApiModelProperty(value = "下单用户id")
    private Long userId;

    /**
     * 地址id
     */
    @ApiModelProperty(value = "地址id")
    private Long addressBookId;

    /**
     * 下单时间
     */
    @ApiModelProperty(value = "下单时间")
    private LocalDateTime orderTime;

    /**
     * 结账时间
     */
    @ApiModelProperty(value = "结账时间")
    private LocalDateTime checkoutTime;

    /**
     * 支付方式 1微信 2支付宝
     */
    @ApiModelProperty(value = "支付方式 1微信 2支付宝")
    private Integer payMethod;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    @ApiModelProperty(value = "支付状态 0未支付 1已支付 2退款")
    private Integer payStatus;

    /**
     * 实收金额
     */
    @ApiModelProperty(value = "实收金额")
    private BigDecimal amount;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 地址
     */
    @ApiModelProperty(value = "地址")
    private String address;

    /**
     * 收货人
     */
    @ApiModelProperty(value = "收货人")
    private String consignee;

    /**
     * 订单取消原因
     */
    @ApiModelProperty(value = "订单取消原因")
    private String cancelReason;

    /**
     * 订单拒绝原因
     */
    @ApiModelProperty(value = "订单拒绝原因")
    private String rejectionReason;

    /**
     * 订单取消时间
     */
    @ApiModelProperty(value = "订单取消时间")
    private LocalDateTime cancelTime;

    /**
     * 预计送达时间
     */
    @ApiModelProperty(value = "预计送达时间")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 配送状态 1立即送出 0选择具体时间
     */
    @ApiModelProperty(value = "配送状态 1立即送出 0选择具体时间")
    private Integer deliveryStatus;

    /**
     * 送达时间
     */
    @ApiModelProperty(value = "送达时间")
    private LocalDateTime deliveryTime;

}
