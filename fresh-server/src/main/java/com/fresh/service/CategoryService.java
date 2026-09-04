package com.fresh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.dto.CategoryAddDTO;
import com.fresh.dto.CategoryPageQueryDTO;
import com.fresh.dto.CategoryUpdateDTO;
import com.fresh.entity.Category;
import com.fresh.result.PageResult;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 新增分类（getById 由 IService 提供，直接使用）
     * @param categoryAddDTO 新增分类时传递的数据
     */
    void save(CategoryAddDTO categoryAddDTO);

    /**
     * 分页查询分类
     * @param categoryPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 修改分类
     * @param categoryUpdateDTO 修改分类时传递的数据
     */
    void update(CategoryUpdateDTO categoryUpdateDTO);

    /**
     * 根据id删除分类（分类下有商品时不允许删除）
     * @param id 分类id
     */
    void delete(Long id);

    /**
     * C端查询所有启用的分类（按 sort、id 升序，不分页）
     * @return 分类列表
     */
    List<Category> listEnabled();
}
