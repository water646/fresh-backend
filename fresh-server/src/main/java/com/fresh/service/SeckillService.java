package com.fresh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.dto.SeckillGoodsAddDTO;
import com.fresh.dto.SeckillGoodsPageQueryDTO;
import com.fresh.dto.SeckillGoodsUpdateDTO;
import com.fresh.entity.SeckillGoods;
import com.fresh.result.PageResult;
import com.fresh.result.Result;

public interface SeckillService extends IService<SeckillGoods> {
    void add(SeckillGoodsAddDTO seckillGoodsAddDTO);

    void update(SeckillGoodsUpdateDTO seckillGoodsUpdateDTO);

    PageResult pageQuery(SeckillGoodsPageQueryDTO seckillGoodsPageQueryDTO);

    Result seckill(Long seckillGoodsId);
}
