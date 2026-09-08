package com.fresh.controller.user;

import com.fresh.dto.ShoppingCartAddDTO;
import com.fresh.entity.ShoppingCart;
import com.fresh.result.Result;
import com.fresh.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端购物车相关接口
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Tag(name = "C端购物车相关接口")
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
    @Operation(summary = "添加商品到购物车")
    public Result add(@Valid @RequestBody ShoppingCartAddDTO shoppingCartAddDTO) {
        log.info("添加购物车：{}", shoppingCartAddDTO);
        shoppingCartService.add(shoppingCartAddDTO);
        return Result.success();
    }

    /**
     * 查询当前用户的购物车列表
     * @return 购物车列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询当前用户的购物车列表")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.list());
    }

    /**
     * 根据id删除购物车中的一条数据（只能删自己的）
     * @param id 购物车条目id
     * @return 成功标识
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除购物车中的一条数据")
    public Result delete(Long id) {
        log.info("删除购物车条目：{}", id);
        shoppingCartService.delete(id);
        return Result.success();
    }
}
