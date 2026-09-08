package com.fresh.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class DoSeckillDTO {
    @NotNull(message = "秒杀商品id不能为空")
    private Long seckillGoodsId;
}
