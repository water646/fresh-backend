package com.fresh.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.fresh.context.BaseContext;
import com.fresh.dto.SeckillOrdersPageQueryDTO;
import com.fresh.dto.SeckillOrdersPayDTO;
import com.fresh.dto.SeckillOrdersUpdateDTO;
import com.fresh.entity.AddressBook;
import com.fresh.entity.SeckillGoods;
import com.fresh.entity.SeckillOrders;
import com.fresh.exception.BaseException;
import com.fresh.exception.OrderStatusException;
import com.fresh.mapper.AddressBookMapper;
import com.fresh.mapper.SeckillOrdersMapper;
import com.fresh.result.PageResult;
import com.fresh.service.SeckillOrdersService;
import com.fresh.service.SeckillService;
import com.fresh.vo.SeckillOrdersVO;
import com.fresh.websocket.WebSocketServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单服务：分页查询 / 修改（删除直接复用 IService 的 removeById）
 */
@Service
@Slf4j
public class SeckillOrdersServiceImpl extends ServiceImpl<SeckillOrdersMapper, SeckillOrders> implements SeckillOrdersService {

    @Autowired
    private SeckillOrdersMapper seckillOrdersMapper;
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 按id查秒杀订单详情（带秒杀商品名称）
     * @param id 秒杀订单id
     * @return 订单 VO（商品被删时 seckillGoodsName 为 null；订单不存在返回 null）
     */
    public SeckillOrdersVO getByIdWithGoods(Long id) {
        SeckillOrders seckillOrders = getById(id);
        if (seckillOrders == null) {
            return null;
        }

        SeckillOrdersVO seckillOrdersVO = new SeckillOrdersVO();
        BeanUtils.copyProperties(seckillOrders, seckillOrdersVO);
        //秒杀商品可能已被删除，查不到名称就保持 null（与分页联表的 LEFT JOIN 行为一致）
        SeckillGoods seckillGoods = seckillService.getById(seckillOrders.getSeckillGoodsId());
        if (seckillGoods != null) {
            seckillOrdersVO.setSeckillGoodsName(seckillGoods.getName());
        }
        return seckillOrdersVO;
    }

    /**
     * 分页查询秒杀订单（联表带出秒杀商品名称）
     * @param seckillOrdersPageQueryDTO 分页查询条件
     * @return 分页结果（records 为 SeckillOrdersVO 列表，按 id 倒序）
     */
    public PageResult pageQuery(SeckillOrdersPageQueryDTO seckillOrdersPageQueryDTO) {
        //设置分页参数；紧跟其后的第一条 SQL（联表查询）会被追加分页
        PageHelper.startPage(seckillOrdersPageQueryDTO.getPage(), seckillOrdersPageQueryDTO.getPageSize());

        //条件过滤写在了 SQL 的 <if> 里：number 模糊、status 精确、phone 模糊、下单时间范围；userId 传 null 查全部
        Page<SeckillOrdersVO> page = (Page<SeckillOrdersVO>) seckillOrdersMapper.listWithGoodsName(
                seckillOrdersPageQueryDTO.getNumber(),
                seckillOrdersPageQueryDTO.getStatus(),
                seckillOrdersPageQueryDTO.getPhone(),
                seckillOrdersPageQueryDTO.getBeginTime(),
                seckillOrdersPageQueryDTO.getEndTime(),
                null);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 分页查询当前用户自己的秒杀订单（用户端，只能查到自己的）
     * @param seckillOrdersPageQueryDTO 分页查询条件（status 可选，其余条件对用户端无效）
     * @return 分页结果（records 为 SeckillOrdersVO 列表，含秒杀商品名称）
     */
    public PageResult pageQueryMine(SeckillOrdersPageQueryDTO seckillOrdersPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 分页查询自己的秒杀订单：{}", userId, seckillOrdersPageQueryDTO);

        PageHelper.startPage(seckillOrdersPageQueryDTO.getPage(), seckillOrdersPageQueryDTO.getPageSize());

        //userId 限定为自己的，status 精确过滤，按 id 倒序（最新在前）
        Page<SeckillOrdersVO> page = (Page<SeckillOrdersVO>) seckillOrdersMapper.listWithGoodsName(
                null,
                seckillOrdersPageQueryDTO.getStatus(),
                null,
                null,
                null,
                userId);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 秒杀订单支付：用地址簿填充收货信息并完成支付
     * @param seckillOrdersPayDTO 订单号 + 地址簿id + 支付方式
     */
    public void pay(SeckillOrdersPayDTO seckillOrdersPayDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 秒杀订单支付：{}", userId, seckillOrdersPayDTO);

        //查订单（不存在或不属于当前用户都按不存在处理，避免越权探测）
        SeckillOrders order = seckillOrdersMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrders>().eq(SeckillOrders::getNumber, seckillOrdersPayDTO.getNumber()));
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BaseException("订单不存在");
        }

        //查地址簿并校验归属（收货人/手机号/详细地址以下单时的快照形式写入订单）
        AddressBook addressBook = addressBookMapper.selectById(seckillOrdersPayDTO.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BaseException("收货地址不存在");
        }

        //填充收货信息 + 支付状态流转：1待付款→2待接单、payStatus 0→1、记录结账时间
        SeckillOrders update = new SeckillOrders();
        update.setPayMethod(seckillOrdersPayDTO.getPayMethod());
        update.setPayStatus(1);
        update.setStatus(2);
        update.setCheckoutTime(LocalDateTime.now());
        update.setAddressBookId(addressBook.getId());
        update.setConsignee(addressBook.getConsignee());
        update.setPhone(addressBook.getPhone());
        update.setAddress(addressBook.getDetail());

        //条件更新：只有待付款的订单才能支付，防止并发/重复支付把已取消订单改回已支付
        LambdaUpdateWrapper<SeckillOrders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SeckillOrders::getId, order.getId())
                .eq(SeckillOrders::getStatus, 1);
        int rows = seckillOrdersMapper.update(update, wrapper);
        if (rows == 0) {
            throw new OrderStatusException("订单已取消或已支付");
        }

        //与普通订单一致：支付成功通过 WebSocket 向商家端推送来单提醒（消息类型 type=1）
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", order.getId());
        map.put("content", "订单号：" + order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 修改秒杀订单（部分更新）
     * @param seckillOrdersUpdateDTO 修改内容（含订单id）
     */
    public void update(SeckillOrdersUpdateDTO seckillOrdersUpdateDTO) {
        log.info("修改秒杀订单：{}", seckillOrdersUpdateDTO);

        SeckillOrders seckillOrders = new SeckillOrders();
        BeanUtils.copyProperties(seckillOrdersUpdateDTO, seckillOrders);
        //MP 的 updateById 默认忽略 null 字段，前端没传的字段不会被覆盖
        seckillOrdersMapper.updateById(seckillOrders);
    }

    /**
     * 落库秒杀订单并扣减数据库库存（MQ 消费者调用，两步在同一事务）
     * @param seckillOrders 秒杀订单
     */
    @Transactional
    public void createOrder(SeckillOrders seckillOrders) {
        //原子扣减数据库库存：stock > 0 才扣（防止扣成负数），返回 false 说明数据库库存不足
        boolean deducted = seckillService.update(
                new LambdaUpdateWrapper<SeckillGoods>()
                        .setSql("stock = stock - 1")
                        .eq(SeckillGoods::getId, seckillOrders.getSeckillGoodsId())
                        .gt(SeckillGoods::getStock, 0));
        if (!deducted) {
            //正常不会走到这里（秒杀前 Redis 已扣过库存），走到说明 Redis 与数据库库存不一致
            log.warn("订单 {} 扣减数据库库存失败（商品 {} 库存不足），订单不落库",
                    seckillOrders.getNumber(), seckillOrders.getSeckillGoodsId());
            return;
        }

        save(seckillOrders);
        log.info("秒杀订单 {} 已写入数据库并扣减库存，订单id：{}", seckillOrders.getNumber(), seckillOrders.getId());
    }
}
