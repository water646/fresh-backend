package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.context.BaseContext;
import com.fresh.dto.ShoppingCartAddDTO;
import com.fresh.entity.Goods;
import com.fresh.entity.ShoppingCart;
import com.fresh.exception.BaseException;
import com.fresh.mapper.GoodsMapper;
import com.fresh.mapper.ShoppingCartMapper;
import com.fresh.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 添加商品到购物车（同一商品累加数量）
     * @param shoppingCartAddDTO 含商品id和数量
     */
    public void add(ShoppingCartAddDTO shoppingCartAddDTO) {
        Long userId = BaseContext.getCurrentId();
        Long goodsId = shoppingCartAddDTO.getGoodsId();
        log.info("用户 {} 添加购物车：{}", userId, shoppingCartAddDTO);

        //添加数量，不传或非法时默认 1
        int count = shoppingCartAddDTO.getNumber() == null || shoppingCartAddDTO.getNumber() < 1
                ? 1 : shoppingCartAddDTO.getNumber();

        //校验商品存在且在售
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getStatus() != 1) {
            throw new BaseException("商品不存在或已下架");
        }

        //同一用户同一商品只存一行，已存在则累加数量
        ShoppingCart cart = shoppingCartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getGoodsId, goodsId));
        if (cart != null) {
            cart.setNumber(cart.getNumber() + count);
            shoppingCartMapper.updateById(cart);
            return;
        }

        //不存在则插入一行，name/image/amount 为加入时的快照（amount 存单价）
        cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setName(goods.getName());
        cart.setImage(goods.getImage());
        cart.setAmount(goods.getPrice());
        cart.setNumber(count);
        //create_time 由数据库默认值填充
        shoppingCartMapper.insert(cart);
    }

    /**
     * 查询当前用户的购物车列表
     * @return 购物车列表（按加入顺序）
     */
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 查询购物车", userId);
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .orderByAsc(ShoppingCart::getId));
    }

    /**
     * 根据id删除当前用户购物车中的一条数据（只能删自己的）
     * @param id 购物车条目id
     */
    public void delete(Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 删除购物车条目：{}", userId, id);
        ShoppingCart cart = shoppingCartMapper.selectById(id);
        //不存在或不属于当前用户都按不存在处理，避免越权探测
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BaseException("购物车数据不存在");
        }
        shoppingCartMapper.deleteById(id);
    }
}
