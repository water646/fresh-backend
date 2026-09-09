package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.context.BaseContext;
import com.fresh.dto.GoodsCommentsAdminPageQueryDTO;
import com.fresh.dto.GoodsCommentsPageQueryDTO;
import com.fresh.dto.GoodsCommentsReplyDTO;
import com.fresh.dto.GoodsCommentsSubmitDTO;
import com.fresh.entity.GoodsComments;
import com.fresh.entity.OrderDetail;
import com.fresh.entity.Orders;
import com.fresh.exception.BaseException;
import com.fresh.mapper.GoodsCommentsMapper;
import com.fresh.mapper.OrderDetailMapper;
import com.fresh.mapper.OrdersMapper;
import com.fresh.result.PageResult;
import com.fresh.service.GoodsCommentsService;
import com.fresh.vo.GoodsCommentsAdminVO;
import com.fresh.vo.GoodsCommentsVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 商品评价服务实现：提交、分页查询（用户端/管理端）、管理端回复（不含商品评分统计）
 */
@Service
@Slf4j
public class GoodsCommentsServiceImpl implements GoodsCommentsService {

    @Autowired
    private GoodsCommentsMapper goodsCommentsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 提交商品评价：明细存在 -> 订单归属当前用户 -> 订单已完成 -> 未评价过，四层校验通过后插入
     * @param goodsCommentsSubmitDTO 订单明细id + 评分 + 内容 + 是否匿名
     */
    public void submit(GoodsCommentsSubmitDTO goodsCommentsSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}提交商品评价：{}", userId, goodsCommentsSubmitDTO);

        //1.查订单明细（评价挂在明细上，一条明细对应一件商品）
        OrderDetail orderDetail = orderDetailMapper.selectById(goodsCommentsSubmitDTO.getOrderDetailId());
        if (orderDetail == null) {
            throw new BaseException("订单明细不存在");
        }

        //2.查订单并校验归属（不属于当前用户按不存在处理，避免越权探测他人订单）
        Orders orders = ordersMapper.selectById(orderDetail.getOrderId());
        if (orders == null || !userId.equals(orders.getUserId())) {
            throw new BaseException("订单不存在");
        }

        //3.只有已完成的订单才能评价
        if (!Orders.COMPLETED.equals(orders.getStatus())) {
            throw new BaseException("订单完成后才能评价");
        }

        //4.防重复评价（预检查，数据库唯一索引 uk_order_detail 兜底并发场景）
        Long count = goodsCommentsMapper.selectCount(new LambdaQueryWrapper<GoodsComments>()
                .eq(GoodsComments::getOrderDetailId, goodsCommentsSubmitDTO.getOrderDetailId()));
        if (count > 0) {
            throw new BaseException("该商品已评价，请勿重复评价");
        }

        //5.组装落库，status 默认显示；createTime/createUser 由 AutoFillAspect 填充（createUser 即用户id）
        GoodsComments goodsComments = new GoodsComments();
        goodsComments.setGoodsId(orderDetail.getGoodsId());
        goodsComments.setUserId(userId);
        goodsComments.setOrderId(orders.getId());
        goodsComments.setOrderDetailId(goodsCommentsSubmitDTO.getOrderDetailId());
        goodsComments.setRating(goodsCommentsSubmitDTO.getRating());
        goodsComments.setContent(goodsCommentsSubmitDTO.getContent());
        goodsComments.setAnonymous(goodsCommentsSubmitDTO.getAnonymous() == null ? 0 : goodsCommentsSubmitDTO.getAnonymous());
        goodsComments.setStatus(1);
        try {
            goodsCommentsMapper.insert(goodsComments);
        } catch (DuplicateKeyException e) {
            //并发下撞唯一索引，与预检查返回同样的提示
            throw new BaseException("该商品已评价，请勿重复评价");
        }
    }

    /**
     * 用户端：分页查询某商品显示中的评价（匿名昵称/头像已在 SQL 里脱敏）
     * @param goodsCommentsPageQueryDTO 商品id + 分页参数
     * @return 分页结果，records 为 GoodsCommentsVO，按评价 id 倒序
     */
    public PageResult pageQuery(GoodsCommentsPageQueryDTO goodsCommentsPageQueryDTO) {
        if (goodsCommentsPageQueryDTO.getGoodsId() == null) {
            throw new BaseException("商品id不能为空");
        }
        log.info("分页查询商品{}的评价：{}", goodsCommentsPageQueryDTO.getGoodsId(), goodsCommentsPageQueryDTO);

        //设置分页参数；紧跟其后的第一条 SQL（联表查询）会被追加分页
        PageHelper.startPage(goodsCommentsPageQueryDTO.getPage(), goodsCommentsPageQueryDTO.getPageSize());
        Page<GoodsCommentsVO> page = (Page<GoodsCommentsVO>) goodsCommentsMapper.listForUser(
                goodsCommentsPageQueryDTO.getGoodsId());

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 管理端：分页查询全部评价（含隐藏的，带商品名与真实用户昵称）
     * @param goodsCommentsAdminPageQueryDTO 分页参数 + 可选过滤条件（goodsId/rating/status 精确，content 模糊）
     * @return 分页结果，records 为 GoodsCommentsAdminVO，按评价 id 倒序
     */
    public PageResult adminPageQuery(GoodsCommentsAdminPageQueryDTO goodsCommentsAdminPageQueryDTO) {
        log.info("管理端分页查询商品评价：{}", goodsCommentsAdminPageQueryDTO);

        PageHelper.startPage(goodsCommentsAdminPageQueryDTO.getPage(), goodsCommentsAdminPageQueryDTO.getPageSize());
        //条件过滤写在了 SQL 的 <if> 里，全部可选
        Page<GoodsCommentsAdminVO> page = (Page<GoodsCommentsAdminVO>) goodsCommentsMapper.listForAdmin(
                goodsCommentsAdminPageQueryDTO.getGoodsId(),
                goodsCommentsAdminPageQueryDTO.getRating(),
                goodsCommentsAdminPageQueryDTO.getStatus(),
                goodsCommentsAdminPageQueryDTO.getContent());

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 管理端：回复商品评价（可重复回复，覆盖旧回复）
     * @param goodsCommentsReplyDTO 评价id + 回复内容
     */
    public void reply(GoodsCommentsReplyDTO goodsCommentsReplyDTO) {
        log.info("员工{}回复商品评价：{}", BaseContext.getCurrentId(), goodsCommentsReplyDTO);

        GoodsComments goodsComments = goodsCommentsMapper.selectById(goodsCommentsReplyDTO.getId());
        if (goodsComments == null) {
            throw new BaseException("评价不存在");
        }

        //只更新回复相关字段；updateTime/updateUser 由 AutoFillAspect 填充（操作人为当前员工）
        GoodsComments update = new GoodsComments();
        update.setId(goodsCommentsReplyDTO.getId());
        update.setReply(goodsCommentsReplyDTO.getReply());
        update.setReplyTime(LocalDateTime.now());
        goodsCommentsMapper.updateById(update);
    }
}
