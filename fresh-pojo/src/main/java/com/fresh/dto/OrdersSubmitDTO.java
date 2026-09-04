package com.fresh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO {
    //地址簿id
    private Long addressBookId;
    //付款方式
    private int payMethod;
    //备注
    private String remark;
    //配送状态  1立即送出  0选择具体时间
    private Integer deliveryStatus;
    //实收金额
    private BigDecimal amount;
}
