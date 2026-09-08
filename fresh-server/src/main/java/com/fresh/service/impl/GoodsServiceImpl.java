package com.fresh.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.constant.RedisConstant;
import com.fresh.dto.GoodsAddDTO;
import com.fresh.dto.GoodsPageQueryDTO;
import com.fresh.dto.GoodsUpdateDTO;
import com.fresh.entity.Goods;
import com.fresh.mapper.GoodsMapper;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.GoodsService;
import com.fresh.vo.GoodsVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper,Goods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增商品
     *
     * @param goodsAddDTO 新增商品时传递的数据
     */
    public void save(GoodsAddDTO goodsAddDTO) {
        log.info("新增商品：{}", goodsAddDTO);
        Goods goods = new Goods();
        //属性拷贝（字段名和类型一致：name/categoryId/price/image/description/status/stock/stockMode）
        BeanUtils.copyProperties(goodsAddDTO, goods);
        //createTime 等公共字段由 AutoFillAspect 在 insert 前自动填充
        goodsMapper.insert(goods);

        //删对应分类的缓存
        stringRedisTemplate.delete(RedisConstant.GOODS_CACHE_KEY +goods.getCategoryId());
    }

    /**
     * 分页查询商品（联表带出分类名称）
     * @param goodsPageQueryDTO 分页查询条件
     * @return 分页结果（records 为 GoodsVO 列表）
     */
    public PageResult pageQuery(GoodsPageQueryDTO goodsPageQueryDTO) {

        String name = goodsPageQueryDTO.getName();
        Integer categoryId = goodsPageQueryDTO.getCategoryId();
        Integer pageNum = goodsPageQueryDTO.getPageNum();
        Integer pageSize = goodsPageQueryDTO.getPageSize();

        //设置分页参数；紧跟其后的第一条 SQL（联表查询）会被追加分页
        PageHelper.startPage(pageNum, pageSize);

        //条件过滤写在了 SQL 的 <if> 里，name 模糊、categoryId 精确；管理端不限制状态
        Page<GoodsVO> page = (Page<GoodsVO>) goodsMapper.listWithCategory(name, categoryId, null);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * C端分页查询在售商品（强制 status=1）
     * @param goodsPageQueryDTO 分页查询条件
     * @return 分页结果（records 为 GoodsVO 列表）
     */
    public PageResult pageQueryOnSale(GoodsPageQueryDTO goodsPageQueryDTO) {
        String name = goodsPageQueryDTO.getName();
        Integer categoryId = goodsPageQueryDTO.getCategoryId();
        Integer pageNum = goodsPageQueryDTO.getPageNum();
        Integer pageSize = goodsPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        //C端只展示在售商品，status 强制传 1，不信任前端
        Page<GoodsVO> page = (Page<GoodsVO>) goodsMapper.listWithCategory(name, categoryId, 1);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * C端根据 id 查询在售商品，不存在或已下架返回 null
     * @param id 商品id
     * @return 商品 VO
     */
    public GoodsVO getOnSaleById(Long id) {
        GoodsVO goodsVO = goodsMapper.getByIdWithCategory(id);
        //已下架的商品对C端视为不存在
        if (goodsVO == null || goodsVO.getStatus() != 1) {
            return null;
        }
        return goodsVO;
    }

    /**
     * C端查询所有在售商品（不分页，按 id 升序）
     * @return 商品 VO 列表
     */
    public List<GoodsVO> listAllOnSale() {
        //不调用 PageHelper，复用联表查询：无名称/分类条件，只限定在售
        return goodsMapper.listWithCategory(null, null, 1);
    }

    /**
     * 根据 id 查询商品（联表带出分类名称）
     * @param id 商品id
     * @return 商品 VO
     */
    public GoodsVO getByIdWithCategory(Long id) {
        log.info("根据id联表查询商品：{}", id);
        return goodsMapper.getByIdWithCategory(id);
    }

    /**
     * 修改商品
     * @param goodsUpdateDTO 修改商品时传递的数据
     */
    public void update(GoodsUpdateDTO goodsUpdateDTO) {
        log.info("修改商品：{}", goodsUpdateDTO);
        Goods oldGoods = getById(goodsUpdateDTO.getId());
        Long oldCategoryId = oldGoods.getCategoryId();

        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsUpdateDTO, goods);
        //updateTime/updateUser 由 AutoFillAspect 在更新前自动填充；
        //MP 的 updateById 默认忽略 null 字段，前端没传的字段不会被覆盖
        goodsMapper.updateById(goods);

        //删除旧的分类和新的分类的缓存
        Long currentCategoryId = goodsUpdateDTO.getCategoryId();
        stringRedisTemplate.delete(RedisConstant.GOODS_CACHE_KEY +currentCategoryId);
        stringRedisTemplate.delete(RedisConstant.GOODS_CACHE_KEY +oldCategoryId);
    }

    /**
     * 根据id删除商品
     * @param id 商品id
     */
    public void delete(Long id) {
        log.info("删除商品：{}", id);
        Long categoryId = getById(id).getCategoryId();
        goodsMapper.deleteById(id);
        stringRedisTemplate.delete(RedisConstant.GOODS_CACHE_KEY +categoryId);
    }

    public List<GoodsVO> listByCategory(Integer categoryId) {
        String cacheGoods = stringRedisTemplate.opsForValue().get(RedisConstant.GOODS_CACHE_KEY +categoryId);
        if(cacheGoods != null){
            List<GoodsVO> goodsList =  JSON.parseArray(cacheGoods,GoodsVO.class);
            return goodsList;
        }

        List<GoodsVO> goodsList = goodsMapper.listWithCategory(null,categoryId,1);
        String goodsJson = JSON.toJSONString(goodsList);

        if(goodsList.size()==0){
            stringRedisTemplate.opsForValue().set(RedisConstant.GOODS_CACHE_KEY +categoryId,"[]",2, TimeUnit.MINUTES);
        }
        else {
            stringRedisTemplate.opsForValue().set(RedisConstant.GOODS_CACHE_KEY +categoryId,goodsJson);
        }


        return goodsList;
    }

    public void startOrStop(GoodsUpdateDTO goodsUpdateDTO) {
        LambdaUpdateWrapper<Goods> uw = new LambdaUpdateWrapper<>();
        uw.eq(Goods::getId, goodsUpdateDTO.getId());
        uw.set(Goods::getStatus,goodsUpdateDTO.getStatus());

        update(null,uw);

        //上下架改变商品是否对C端可见，需删除所属分类的缓存（与增删改保持一致），否则listByCategory会一直返回旧数据
        Goods goods = getById(goodsUpdateDTO.getId());
        if (goods != null) {
            stringRedisTemplate.delete(RedisConstant.GOODS_CACHE_KEY +goods.getCategoryId());
        }
    }
}
