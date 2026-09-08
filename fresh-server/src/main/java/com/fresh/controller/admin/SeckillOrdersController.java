package com.fresh.controller.admin;

import com.fresh.dto.SeckillOrdersPageQueryDTO;
import com.fresh.dto.SeckillOrdersUpdateDTO;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.SeckillOrdersService;
import com.fresh.vo.SeckillOrdersVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀订单管理（管理端）
 */
@RestController
@RequestMapping("/admin/seckillOrders")
@Tag(name = "秒杀订单管理接口")
@Slf4j
public class SeckillOrdersController {

    @Autowired
    private SeckillOrdersService seckillOrdersService;

    /**
     * 分页查询秒杀订单
     * @param seckillOrdersPageQueryDTO 分页查询条件（GET 查询参数绑定）
     * @return 分页结果，records 为 SeckillOrdersVO（多带秒杀商品名称）
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询秒杀订单")
    public Result<PageResult> page(SeckillOrdersPageQueryDTO seckillOrdersPageQueryDTO) {
        log.info("分页查询秒杀订单：{}", seckillOrdersPageQueryDTO);
        PageResult pageResult = seckillOrdersService.pageQuery(seckillOrdersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改秒杀订单
     * @param seckillOrdersUpdateDTO 修改内容（JSON 请求体，含订单id）
     * @return 成功提示
     */
    @PutMapping("/update")
    @Operation(summary = "修改秒杀订单")
    public Result update(@Valid @RequestBody SeckillOrdersUpdateDTO seckillOrdersUpdateDTO) {
        log.info("修改秒杀订单：{}", seckillOrdersUpdateDTO);
        seckillOrdersService.update(seckillOrdersUpdateDTO);
        return Result.success();
    }

    /**
     * 根据id查询秒杀订单详情
     * @param id 秒杀订单id
     * @return 订单 VO（多带秒杀商品名称；订单不存在时 data 为 null）
     */
    @GetMapping("/get")
    @Operation(summary = "根据id查询秒杀订单")
    public Result<SeckillOrdersVO> get(Long id) {
        log.info("查询秒杀订单详情，id：{}", id);
        SeckillOrdersVO seckillOrdersVO = seckillOrdersService.getByIdWithGoods(id);
        return Result.success(seckillOrdersVO);
    }

    /**
     * 根据id删除秒杀订单
     * @param id 秒杀订单id
     * @return 成功提示
     */
    @DeleteMapping("/delete")
    @Operation(summary = "根据id删除秒杀订单")
    public Result delete(Long id) {
        log.info("删除秒杀订单，id：{}", id);
        seckillOrdersService.removeById(id);
        return Result.success();
    }
}
