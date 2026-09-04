package com.fresh.controller.user;

import com.fresh.dto.ShoppingCartAddDTO;
import com.fresh.entity.ShoppingCart;
import com.fresh.result.Result;
import com.fresh.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端购物车相关接口
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端购物车相关接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     * @param shoppingCartAddDTO 含商品id和数量
     * @return 成功标识
     */
    @PostMapping("/add")
    @ApiOperation("添加商品到购物车")
    public Result add(@RequestBody ShoppingCartAddDTO shoppingCartAddDTO) {
        log.info("添加购物车：{}", shoppingCartAddDTO);
        shoppingCartService.add(shoppingCartAddDTO);
        return Result.success();
    }

    /**
     * 查询当前用户的购物车列表
     * @return 购物车列表
     */
    @GetMapping("/list")
    @ApiOperation("查询当前用户的购物车列表")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.list());
    }

    /**
     * 根据id删除购物车中的一条数据（只能删自己的）
     * @param id 购物车条目id
     * @return 成功标识
     */
    @DeleteMapping("/delete")
    @ApiOperation("删除购物车中的一条数据")
    public Result delete(Long id) {
        log.info("删除购物车条目：{}", id);
        shoppingCartService.delete(id);
        return Result.success();
    }
}
