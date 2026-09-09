package com.fresh.service;

import com.fresh.dto.GoodsCommentsAdminPageQueryDTO;
import com.fresh.dto.GoodsCommentsPageQueryDTO;
import com.fresh.dto.GoodsCommentsReplyDTO;
import com.fresh.dto.GoodsCommentsSubmitDTO;
import com.fresh.result.PageResult;

/**
 * 商品评价服务接口
 */
public interface GoodsCommentsService {

    /**
     * 提交商品评价：校验订单已完成且属于当前用户、明细未评价过，通过后落库
     * @param goodsCommentsSubmitDTO 订单明细id + 评分 + 内容 + 是否匿名
     */
    void submit(GoodsCommentsSubmitDTO goodsCommentsSubmitDTO);

    /**
     * 用户端：分页查询某商品显示中的评价（商品详情页评价列表，匿名已脱敏）
     * @param goodsCommentsPageQueryDTO 商品id + 分页参数
     * @return 分页结果，records 为 GoodsCommentsVO
     */
    PageResult pageQuery(GoodsCommentsPageQueryDTO goodsCommentsPageQueryDTO);

    /**
     * 管理端：分页查询全部评价（含隐藏的，带商品名与真实用户昵称）
     * @param goodsCommentsAdminPageQueryDTO 分页参数 + 可选过滤条件
     * @return 分页结果，records 为 GoodsCommentsAdminVO
     */
    PageResult adminPageQuery(GoodsCommentsAdminPageQueryDTO goodsCommentsAdminPageQueryDTO);

    /**
     * 管理端：回复商品评价（可重复回复，覆盖旧回复）
     * @param goodsCommentsReplyDTO 评价id + 回复内容
     */
    void reply(GoodsCommentsReplyDTO goodsCommentsReplyDTO);
}
