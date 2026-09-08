package com.fresh.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Data
public class OrdersPaymentDTO implements Serializable {
    //订单号
    @NotBlank(message = "订单号不能为空")
    @Size(max = 50, message = "订单号长度不能超过50")
    private String orderNumber;

    //付款方式
    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式只能为1或2")
    @Max(value = 2, message = "支付方式只能为1或2")
    private Integer payMethod;

}