package com.fresh.service;

import com.fresh.dto.ShoppingCartAddDTO;
import com.fresh.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加商品到购物车（同一商品累加数量）
     * @param shoppingCartAddDTO 含商品id和数量
     */
    void add(ShoppingCartAddDTO shoppingCartAddDTO);

    /**
     * 查询当前用户的购物车列表
     * @return 购物车列表（按加入顺序）
     */
    List<ShoppingCart> list();

    /**
     * 根据id删除当前用户购物车中的一条数据（只能删自己的）
     * @param id 购物车条目id
     */
    void delete(Long id);
}
