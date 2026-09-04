package com.fresh.dto;

import lombok.Data;

@Data
public class SeckillGoodsUpdateDTO extends SeckillGoodsAddDTO{

    /**
     * 秒杀商品id（Long：与实体一致，Integer 会导致 BeanUtils 拷贝失败）
     */
    private Long id;

    private Integer status;

}
