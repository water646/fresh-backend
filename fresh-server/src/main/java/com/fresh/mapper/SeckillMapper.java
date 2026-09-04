package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.fresh.annotation.AutoFill;
import com.fresh.entity.Goods;
import com.fresh.entity.SeckillGoods;
import com.fresh.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeckillMapper extends BaseMapper<SeckillGoods> {

    @Override
    @AutoFill(OperationType.INSERT)
    int insert(SeckillGoods seckillGoods);

    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param(Constants.ENTITY) SeckillGoods seckillGoods);
}
