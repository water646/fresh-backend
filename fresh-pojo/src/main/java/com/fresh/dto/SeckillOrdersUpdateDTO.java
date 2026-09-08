package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 修改秒杀订单时传递的数据（部分更新：只改传了的字段，没传的保持原值）
 * 校验注解均为"传了才校验"型（null 直接通过），与部分更新语义匹配
 */
@Data
@Schema(name = "SeckillOrdersUpdateDTO", description = "修改秒杀订单时传递的数据（部分更新）")
public class SeckillOrdersUpdateDTO implements Serializable {

    /**
     * 秒杀订单id
     */
    @NotNull(message = "秒杀订单id不能为空")
    @Schema(description = "秒杀订单id", required = true)
    private Long id;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @Min(value = 1, message = "订单状态只能为1~6")
    @Max(value = 6, message = "订单状态只能为1~6")
    @Schema(description = "订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消")
    private Integer status;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    @Min(value = 0, message = "支付状态只能为0、1或2")
    @Max(value = 2, message = "支付状态只能为0、1或2")
    @Schema(description = "支付状态 0未支付 1已支付 2退款")
    private Integer payStatus;

    /**
     * 支付方式 1微信 2支付宝
     */
    @Min(value = 1, message = "支付方式只能为1或2")
    @Max(value = 2, message = "支付方式只能为1或2")
    @Schema(description = "支付方式 1微信 2支付宝")
    private Integer payMethod;

    /**
     * 实收金额
     */
    @DecimalMin(value = "0.01", message = "实收金额不能低于0.01元")
    @Digits(integer = 8, fraction = 2, message = "实收金额整数部分最多8位、小数部分最多2位")
    @Schema(description = "实收金额")
    private BigDecimal amount;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;

    /**
     * 用户名
     */
    @Size(max = 50, message = "用户名长度不能超过50")
    @Schema(description = "用户名")
    private String userName;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 地址
     */
    @Size(max = 255, message = "地址长度不能超过255")
    @Schema(description = "地址")
    private String address;

    /**
     * 收货人
     */
    @Size(max = 50, message = "收货人长度不能超过50")
    @Schema(description = "收货人")
    private String consignee;

    /**
     * 订单取消原因
     */
    @Size(max = 255, message = "取消原因长度不能超过255")
    @Schema(description = "订单取消原因")
    private String cancelReason;

    /**
     * 订单拒绝原因
     */
    @Size(max = 255, message = "拒绝原因长度不能超过255")
    @Schema(description = "订单拒绝原因")
    private String rejectionReason;

    /**
     * 预计送达时间（JSON 提交，格式 yyyy-MM-dd HH:mm）
     */
    @Schema(description = "预计送达时间，格式 yyyy-MM-dd HH:mm")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 配送状态 1立即送出 0选择具体时间
     */
    @Min(value = 0, message = "配送状态只能为0或1")
    @Max(value = 1, message = "配送状态只能为0或1")
    @Schema(description = "配送状态 1立即送出 0选择具体时间")
    private Integer deliveryStatus;

}
