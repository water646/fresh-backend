package com.fresh.controller.admin;

import com.fresh.dto.GoodsCommentsAdminPageQueryDTO;
import com.fresh.dto.GoodsCommentsReplyDTO;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.GoodsCommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端商品评价相关接口
 */
@RestController("adminGoodsCommentsController")
@RequestMapping("/admin/goodsComments")
@Tag(name = "管理端商品评价相关接口")
@Slf4j
public class GoodsCommentsController {

    @Autowired
    private GoodsCommentsService goodsCommentsService;

    /**
     * 分页查询商品评价（含隐藏的，带商品名与真实用户昵称）
     * @param goodsCommentsAdminPageQueryDTO 分页参数 + 可选过滤条件（goodsId/rating/status 精确，content 模糊）
     * @return 分页结果，records 为 GoodsCommentsAdminVO
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询商品评价")
    public Result<PageResult> page(GoodsCommentsAdminPageQueryDTO goodsCommentsAdminPageQueryDTO) {
        log.info("管理端分页查询商品评价：{}", goodsCommentsAdminPageQueryDTO);
        return Result.success(goodsCommentsService.adminPageQuery(goodsCommentsAdminPageQueryDTO));
    }

    /**
     * 回复商品评价（可重复回复，覆盖旧回复）
     * @param goodsCommentsReplyDTO 评价id + 回复内容
     * @return 成功提示
     */
    @PutMapping("/reply")
    @Operation(summary = "回复商品评价")
    public Result reply(@Valid @RequestBody GoodsCommentsReplyDTO goodsCommentsReplyDTO) {
        log.info("回复商品评价：{}", goodsCommentsReplyDTO);
        goodsCommentsService.reply(goodsCommentsReplyDTO);
        return Result.success();
    }
}
