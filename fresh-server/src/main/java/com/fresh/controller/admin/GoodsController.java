package com.fresh.controller.admin;

import com.fresh.dto.GoodsAddDTO;
import com.fresh.dto.GoodsPageQueryDTO;
import com.fresh.dto.GoodsUpdateDTO;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.GoodsService;
import com.fresh.vo.GoodsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理
 */
@RestController
@RequestMapping("/admin/goods")
@Api(tags = "商品相关接口")
@Slf4j
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 新增商品
     * @param goodsAddDTO 新增商品时传递的数据
     * @return 成功标识
     */
    @PostMapping("/add")
    @ApiOperation("新增商品")
    public Result save(@RequestBody GoodsAddDTO goodsAddDTO) {
        log.info("新增商品：{}", goodsAddDTO);
        goodsService.save(goodsAddDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询商品")
    public Result<PageResult> page(GoodsPageQueryDTO goodsPageQueryDTO) {
        return Result.success(goodsService.pageQuery(goodsPageQueryDTO));
    }

    /**
     * 根据id查询商品（返回带分类名称的 VO）
     * @param id 商品id
     * @return 查询到的商品 VO
     */
    @GetMapping("/get")
    @ApiOperation("根据id查询商品")
    public Result<GoodsVO> get(Long id) {
        log.info("根据id查询商品：{}", id);
        return Result.success(goodsService.getByIdWithCategory(id));
    }

    /**
     * 修改商品
     * @param goodsUpdateDTO 修改商品时传递的数据
     * @return 成功标识
     */
    @PostMapping("/update")
    @ApiOperation("修改商品")
    public Result update(@RequestBody GoodsUpdateDTO goodsUpdateDTO) {
        log.info("修改商品：{}", goodsUpdateDTO);
        goodsService.update(goodsUpdateDTO);
        return Result.success();
    }

    /**
     * 根据id删除商品
     * @param id 商品id
     * @return 成功标识
     */
    @DeleteMapping("/delete")
    @ApiOperation("根据id删除商品")
    public Result delete(Long id) {
        log.info("根据id删除商品：{}", id);
        goodsService.delete(id);
        return Result.success();
    }

    @PutMapping("/startOrStop")
    @ApiOperation("根据id起售/停售商品")
    public Result startOrStop(@RequestBody GoodsUpdateDTO goodsUpdateDTO) {
        goodsService.startOrStop(goodsUpdateDTO);
        return Result.success();
    }
}
