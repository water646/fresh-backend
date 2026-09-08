package com.fresh.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillGoodsAddDTO {

    /**
     * 秒杀商品名称（数据库 varchar(20)）
     */
    @NotBlank(message = "秒杀商品名称不能为空")
    @Size(max = 20, message = "秒杀商品名称长度不能超过20")
    private String name;

    /**
     * 秒杀价格
     */
    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格不能低于0.01元")
    @Digits(integer = 8, fraction = 2, message = "秒杀价格整数部分最多8位、小数部分最多2位")
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 0, message = "秒杀库存不能为负数")
    private Integer stock;

    /**
     * 每人限购数量
     */
    @Min(value = 1, message = "限购数量至少为1")
    private Integer limitNum;

    /**
     * 秒杀开始时间
     */
    @NotNull(message = "秒杀开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    @NotNull(message = "秒杀结束时间不能为空")
    private LocalDateTime endTime;
}
