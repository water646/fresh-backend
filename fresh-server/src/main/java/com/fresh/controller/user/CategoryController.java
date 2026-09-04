package com.fresh.controller.user;

import com.fresh.entity.Category;
import com.fresh.result.Result;
import com.fresh.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端分类相关接口（仅暴露启用的分类）
 * 与管理端 CategoryController 类名相同，需显式指定 bean 名避免冲突
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "C端分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有启用的分类（按 sort 升序，不分页）
     * @return 分类列表
     */
    @GetMapping("/list")
    @ApiOperation("查询所有启用的分类")
    public Result<List<Category>> list() {
        log.info("C端查询所有启用的分类");
        return Result.success(categoryService.listEnabled());
    }
}
