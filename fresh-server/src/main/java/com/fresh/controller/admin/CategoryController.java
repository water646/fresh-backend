package com.fresh.controller.admin;

import com.fresh.dto.CategoryAddDTO;
import com.fresh.dto.CategoryPageQueryDTO;
import com.fresh.dto.CategoryUpdateDTO;
import com.fresh.entity.Category;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryAddDTO 新增分类时传递的数据
     * @return 成功标识
     */
    @PostMapping("/add")
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryAddDTO categoryAddDTO) {
        log.info("新增分类：{}", categoryAddDTO);
        categoryService.save(categoryAddDTO);
        return Result.success();
    }

    /**
     * 分页查询分类
     * @param categoryPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        return Result.success(categoryService.pageQuery(categoryPageQueryDTO));
    }

    /**
     * 根据id查询分类
     * @param id 分类id
     * @return 查询到的分类数据
     */
    @GetMapping("/get")
    @ApiOperation("根据id查询分类")
    public Result<Category> get(Long id) {
        log.info("根据id查询分类：{}", id);
        return Result.success(categoryService.getById(id));
    }

    /**
     * 修改分类
     * @param categoryUpdateDTO 修改分类时传递的数据
     * @return 成功标识
     */
    @PostMapping("/update")
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        log.info("修改分类：{}", categoryUpdateDTO);
        categoryService.update(categoryUpdateDTO);
        return Result.success();
    }

    /**
     * 根据id删除分类
     * @param id 分类id
     * @return 成功标识
     */
    @DeleteMapping("/delete")
    @ApiOperation("根据id删除分类")
    public Result delete(Long id) {
        log.info("根据id删除分类：{}", id);
        categoryService.delete(id);
        return Result.success();
    }
}
