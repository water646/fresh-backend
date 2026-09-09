package com.fresh.controller.user;

import com.fresh.dto.GoodsCommentsPageQueryDTO;
import com.fresh.dto.GoodsCommentsSubmitDTO;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.GoodsCommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端商品评价相关接口
 */
@RestController("userGoodsCommentsController")
@RequestMapping("/user/goodsComments")
@Tag(name = "用户端商品评价相关接口")
@Slf4j
public class GoodsCommentsController {

    @Autowired
    private GoodsCommentsService goodsCommentsService;

    /**
     * 提交商品评价（订单完成后，对订单中某件商品评分+评论）
     * @param goodsCommentsSubmitDTO 订单明细id + 评分(1-5) + 内容 + 是否匿名
     * @return 成功提示
     */
    @PostMapping("/submit")
    @Operation(summary = "提交商品评价")
    public Result submit(@Valid @RequestBody GoodsCommentsSubmitDTO goodsCommentsSubmitDTO) {
        log.info("提交商品评价：{}", goodsCommentsSubmitDTO);
        goodsCommentsService.submit(goodsCommentsSubmitDTO);
        return Result.success();
    }

    /**
     * 分页查询商品评价（商品详情页评价列表，只含显示中的，匿名已脱敏）
     * @param goodsCommentsPageQueryDTO 商品id + 分页参数（GET 查询参数绑定）
     * @return 分页结果，records 为 GoodsCommentsVO
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询商品评价")
    public Result<PageResult> page(GoodsCommentsPageQueryDTO goodsCommentsPageQueryDTO) {
        log.info("分页查询商品评价：{}", goodsCommentsPageQueryDTO);
        return Result.success(goodsCommentsService.pageQuery(goodsCommentsPageQueryDTO));
    }
}
