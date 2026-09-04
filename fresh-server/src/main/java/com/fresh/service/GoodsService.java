package com.fresh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.dto.GoodsAddDTO;
import com.fresh.dto.GoodsPageQueryDTO;
import com.fresh.dto.GoodsUpdateDTO;
import com.fresh.entity.Goods;
import com.fresh.result.PageResult;
import com.fresh.vo.GoodsVO;

import java.util.List;

public interface GoodsService extends IService<Goods> {

    /**
     * 新增商品
     * @param goodsAddDTO 新增商品时传递的数据
     */
    void save(GoodsAddDTO goodsAddDTO);

    /**
     * 分页查询商品（联表带出分类名称，返回 VO）
     * @param goodsPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQuery(GoodsPageQueryDTO goodsPageQueryDTO);

    /**
     * 根据 id 查询商品（联表带出分类名称，返回 VO）
     * @param id 商品id
     * @return 商品 VO
     */
    GoodsVO getByIdWithCategory(Long id);

    /**
     * C端分页查询在售商品（强制 status=1）
     * @param goodsPageQueryDTO 分页查询条件
     * @return 分页结果（records 为 GoodsVO 列表）
     */
    PageResult pageQueryOnSale(GoodsPageQueryDTO goodsPageQueryDTO);

    /**
     * C端根据 id 查询在售商品，不存在或已下架返回 null
     * @param id 商品id
     * @return 商品 VO
     */
    GoodsVO getOnSaleById(Long id);

    /**
     * C端查询所有在售商品（不分页，按 id 升序）
     * @return 商品 VO 列表
     */
    List<GoodsVO> listAllOnSale();

    /**
     * 修改商品
     * @param goodsUpdateDTO 修改商品时传递的数据
     */
    void update(GoodsUpdateDTO goodsUpdateDTO);

    /**
     * 根据id删除商品
     * @param id 商品id
     */
    void delete(Long id);

    List<GoodsVO> listByCategory(Integer categoryId);

    void startOrStop(GoodsUpdateDTO goodsUpdateDTO);
}
