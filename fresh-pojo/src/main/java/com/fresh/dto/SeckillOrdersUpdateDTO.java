package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 修改秒杀订单时传递的数据（部分更新：只改传了的字段，没传的保持原值）
 */
@Data
@ApiModel(value = "SeckillOrdersUpdateDTO", description = "修改秒杀订单时传递的数据（部分更新）")
public class SeckillOrdersUpdateDTO implements Serializable {

    /**
     * 秒杀订单id
     */
    @ApiModelProperty(value = "秒杀订单id", required = true)
    private Long id;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @ApiModelProperty(value = "订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消")
    private Integer status;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    @ApiModelProperty(value = "支付状态 0未支付 1已支付 2退款")
    private Integer payStatus;

    /**
     * 支付方式 1微信 2支付宝
     */
    @ApiModelProperty(value = "支付方式 1微信 2支付宝")
    private Integer payMethod;

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
     * 预计送达时间（JSON 提交，格式 yyyy-MM-dd HH:mm）
     */
    @ApiModelProperty(value = "预计送达时间，格式 yyyy-MM-dd HH:mm")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 配送状态 1立即送出 0选择具体时间
     */
    @ApiModelProperty(value = "配送状态 1立即送出 0选择具体时间")
    private Integer deliveryStatus;

}
