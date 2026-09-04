package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.constant.MessageConstant;
import com.fresh.dto.CategoryAddDTO;
import com.fresh.dto.CategoryPageQueryDTO;
import com.fresh.dto.CategoryUpdateDTO;
import com.fresh.entity.Category;
import com.fresh.entity.Goods;
import com.fresh.exception.BaseException;
import com.fresh.exception.DeletionNotAllowedException;
import com.fresh.mapper.CategoryMapper;
import com.fresh.mapper.GoodsMapper;
import com.fresh.result.PageResult;
import com.fresh.service.CategoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 新增分类
     * @param categoryAddDTO 新增分类时传递的数据
     */
    public void save(CategoryAddDTO categoryAddDTO) {
        log.info("新增分类：{}", categoryAddDTO);
        //分类名称唯一校验
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, categoryAddDTO.getName()));
        if (count > 0) {
            throw new BaseException(categoryAddDTO.getName() + MessageConstant.ALREADY_EXISTS);
        }

        Category category = new Category();
        //属性拷贝（字段名和类型一致：name/sort/status）
        BeanUtils.copyProperties(categoryAddDTO, category);
        //未传时给默认值
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        //createTime 等公共字段由 AutoFillAspect 在 insert 前自动填充
        categoryMapper.insert(category);
    }

    /**
     * 分页查询分类（按 sort、id 升序）
     * @param categoryPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        String name = categoryPageQueryDTO.getName();
        Integer pageNum = categoryPageQueryDTO.getPageNum();
        Integer pageSize = categoryPageQueryDTO.getPageSize();

        //设置分页参数
        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            qw.like(Category::getName, name);
        }
        qw.orderByAsc(Category::getSort, Category::getId);

        Page<Category> page = (Page<Category>) categoryMapper.selectList(qw);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改分类
     * @param categoryUpdateDTO 修改分类时传递的数据
     */
    public void update(CategoryUpdateDTO categoryUpdateDTO) {
        log.info("修改分类：{}", categoryUpdateDTO);
        //分类名称唯一校验（排除自身）
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, categoryUpdateDTO.getName())
                .ne(Category::getId, categoryUpdateDTO.getId()));
        if (count > 0) {
            throw new BaseException(categoryUpdateDTO.getName() + MessageConstant.ALREADY_EXISTS);
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryUpdateDTO, category);
        //updateTime/updateUser 由 AutoFillAspect 在更新前自动填充；
        //MP 的 updateById 默认忽略 null 字段，前端没传的字段不会被覆盖
        categoryMapper.updateById(category);
    }

    /**
     * 根据id删除分类（分类下有商品时不允许删除）
     * @param id 分类id
     */
    public void delete(Long id) {
        log.info("删除分类：{}", id);
        Long count = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getCategoryId, id));
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_HAS_GOODS);
        }
        categoryMapper.deleteById(id);
    }

    /**
     * C端查询所有启用的分类（按 sort、id 升序，不分页）
     * @return 分类列表
     */
    public List<Category> listEnabled() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort, Category::getId));
    }
}
