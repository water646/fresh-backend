package com.fresh.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 修改秒杀商品时传递的数据
 * 部分更新语义：除 id 外字段均可选，只更新传了的字段（启用/禁用只传 id + status），
 * 因此不继承 SeckillGoodsAddDTO，避免带上"名称/价格/时间必填"等新增时的校验
 */
@Data
public class SeckillGoodsUpdateDTO {

    /**
     * 秒杀商品id（Long：与实体一致，Integer 会导致 BeanUtils 拷贝失败）
     */
    @NotNull(message = "秒杀商品id不能为空")
    private Long id;

    /**
     * 秒杀商品名称（数据库 varchar(20)）
     */
    @Size(max = 20, message = "秒杀商品名称长度不能超过20")
    private String name;

    /**
     * 秒杀价格
     */
    @DecimalMin(value = "0.01", message = "秒杀价格不能低于0.01元")
    @Digits(integer = 8, fraction = 2, message = "秒杀价格整数部分最多8位、小数部分最多2位")
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
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
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态 0禁用 1启用（启用/禁用秒杀商品用）
     */
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
}
