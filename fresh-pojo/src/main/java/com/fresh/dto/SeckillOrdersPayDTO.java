package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 秒杀订单支付（填写收货地址并支付）时传递的数据
 */
@Data
@Schema(name = "SeckillOrdersPayDTO", description = "秒杀订单支付时传递的数据")
public class SeckillOrdersPayDTO implements Serializable {

    /**
     * 订单号（从"查询自己的秒杀订单"接口获得）
     */
    @NotBlank(message = "订单号不能为空")
    @Size(max = 50, message = "订单号长度不能超过50")
    @Schema(description = "订单号", required = true)
    private String number;

    /**
     * 收货地址 id（地址簿接口拿到的 id，须是当前用户自己的地址）
     */
    @NotNull(message = "收货地址id不能为空")
    @Schema(description = "收货地址id", required = true)
    private Long addressBookId;

    /**
     * 支付方式 1微信 2支付宝
     */
    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式只能为1或2")
    @Max(value = 2, message = "支付方式只能为1或2")
    @Schema(description = "支付方式 1微信 2支付宝", required = true)
    private Integer payMethod;

}
