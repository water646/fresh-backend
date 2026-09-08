package com.fresh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO {
    //地址簿id
    @NotNull(message = "收货地址id不能为空")
    private Long addressBookId;
    //付款方式（原始类型不传为0，会被 @Min(1) 拦下）
    @Min(value = 1, message = "支付方式只能为1或2")
    @Max(value = 2, message = "支付方式只能为1或2")
    private int payMethod;
    //备注
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
    //配送状态  1立即送出  0选择具体时间
    @Min(value = 0, message = "配送状态只能为0或1")
    @Max(value = 1, message = "配送状态只能为0或1")
    private Integer deliveryStatus;
    //实收金额
    @NotNull(message = "实收金额不能为空")
    @DecimalMin(value = "0.01", message = "实收金额不能低于0.01元")
    @Digits(integer = 8, fraction = 2, message = "实收金额整数部分最多8位、小数部分最多2位")
    private BigDecimal amount;
}
