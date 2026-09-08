package com.fresh.controller.user;

import com.fresh.dto.SeckillOrdersPageQueryDTO;
import com.fresh.dto.SeckillOrdersPayDTO;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.SeckillOrdersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * C端秒杀订单相关接口：查自己的秒杀订单、填地址并支付
 */
@RestController("userSeckillOrdersController")
@RequestMapping("/user/seckillOrders")
@Tag(name = "C端秒杀订单相关接口")
@Slf4j
public class SeckillOrdersController {

    @Autowired
    private SeckillOrdersService seckillOrdersService;

    /**
     * 分页查询当前用户自己的秒杀订单
     * @param seckillOrdersPageQueryDTO 分页查询条件（page/pageSize 必传，status 可选）
     * @return 分页结果，records 为 SeckillOrdersVO（多带秒杀商品名称）
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询自己的秒杀订单")
    public Result<PageResult> page(SeckillOrdersPageQueryDTO seckillOrdersPageQueryDTO) {
        log.info("分页查询自己的秒杀订单：{}", seckillOrdersPageQueryDTO);
        PageResult pageResult = seckillOrdersService.pageQueryMine(seckillOrdersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 秒杀订单支付（填写收货地址并支付）
     * @param seckillOrdersPayDTO 订单号 + 地址簿id + 支付方式（JSON 请求体）
     * @return 成功提示
     */
    @PutMapping("/payment")
    @Operation(summary = "秒杀订单支付（填写收货地址并支付）")
    public Result payment(@Valid @RequestBody SeckillOrdersPayDTO seckillOrdersPayDTO) {
        log.info("秒杀订单支付：{}", seckillOrdersPayDTO);
        //防重复支付锁在 service 层（pay 内），controller 只做转发
        seckillOrdersService.pay(seckillOrdersPayDTO);
        return Result.success();
    }

    /**
     * 取消秒杀订单（参考普通订单取消：只有待付款的订单可以取消，取消后回补库存）
     * @param id 秒杀订单id
     * @return 成功提示
     */
    @GetMapping("/cancel")
    @Operation(summary = "取消秒杀订单")
    public Result cancel(Long id) {
        log.info("取消秒杀订单：{}", id);
        seckillOrdersService.cancel(id);
        return Result.success();
    }
}
