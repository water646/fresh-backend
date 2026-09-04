package com.fresh.controller.user;

import com.fresh.dto.GoodsPageQueryDTO;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.GoodsService;
import com.fresh.vo.GoodsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端商品相关接口（仅暴露在售商品）
 * 与管理端 GoodsController 类名相同，需显式指定 bean 名避免冲突
 */
@RestController("userGoodsController")
@RequestMapping("/user/goods")
@Api(tags = "C端商品相关接口")
@Slf4j
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询在售商品
     * @param goodsPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询在售商品")
    public Result<PageResult> page(GoodsPageQueryDTO goodsPageQueryDTO) {
        log.info("C端分页查询在售商品：{}", goodsPageQueryDTO);
        return Result.success(goodsService.pageQueryOnSale(goodsPageQueryDTO));
    }

    /**
     * 根据id查询在售商品详情
     * @param id 商品id
     * @return 商品 VO（不存在或已下架时 data 为 null）
     */
    @GetMapping("/get")
    @ApiOperation("根据id查询在售商品")
    public Result<GoodsVO> get(Long id) {
        log.info("C端根据id查询在售商品：{}", id);
        return Result.success(goodsService.getOnSaleById(id));
    }

    /**
     * 查询所有在售商品（不分页）
     * @return 商品 VO 列表
     */
    @GetMapping("/list")
    @ApiOperation("查询所有在售商品")
    public Result<List<GoodsVO>> list() {
        log.info("C端查询所有在售商品");
        return Result.success(goodsService.listAllOnSale());
    }

    /**
     * 按分类查询在售商品（不分页，结果缓存到 Redis）
     * @param categoryId 分类id，不传或传 0 时返回全部在售商品
     * @return 商品 VO 列表
     */
    @GetMapping("/listByCategory")
    @ApiOperation("按分类查询在售商品")
    public Result<List<GoodsVO>> listByCategory(Integer categoryId) {
        log.info("C端按分类查询在售商品：{}", categoryId);
        return Result.success(goodsService.listByCategory(categoryId));
    }
}
