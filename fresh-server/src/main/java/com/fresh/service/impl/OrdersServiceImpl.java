package com.fresh.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.constant.RedisConstant;
import com.fresh.context.BaseContext;
import com.fresh.dto.OrdersPageQueryDTO;
import com.fresh.dto.OrdersSubmitDTO;
import com.fresh.entity.*;
import com.fresh.exception.BaseException;
import com.fresh.exception.OrderStatusException;
import com.fresh.exception.StockInsufficientException;
import com.fresh.mapper.*;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.AddressBookService;
import com.fresh.service.OrdersService;
import com.fresh.vo.OrderVO;
import com.fresh.websocket.WebSocketServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper,Orders> implements OrdersService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Transactional
    public Orders submitOrder(OrdersSubmitDTO ordersSubmitDTO){
        Long userId = BaseContext.getCurrentId();
        Orders orders = new Orders();

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RedisConstant.ORDER_SEQUENCE_KEY + date;

        Long sequence = stringRedisTemplate.opsForValue().increment(key);
        String orderNumber = date + String.format("%08d", sequence);

        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        User user = userMapper.selectById(userId);

        //地址不存在（或不属于当前用户）时给出业务提示，避免空指针
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BaseException("收货地址不存在");
        }

        //向订单表插入一条数据
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setUserId(userId);
        orders.setNumber(orderNumber);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));
        orders.setPayStatus(Orders.PAY_STATUS_UNPAID);
        orders.setAddressBookId(addressBook.getId());
        orders.setUserName(user.getName());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        ordersMapper.insert(orders);

        //向订单明细表插入N条数据，对有库存的商品扣减相应库存
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<ShoppingCart>();
        qw.eq(ShoppingCart::getUserId,userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(qw);
        for(ShoppingCart cart: shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);

            orderDetail.setOrderId(orders.getId());
            orderDetailMapper.insert(orderDetail);


            Long goodsId = cart.getGoodsId();
            Goods goods = goodsMapper.selectById(goodsId);
            //库存充足才允许扣减
            if(goods.getStockMode()==1){
                LambdaUpdateWrapper<Goods> uw = new LambdaUpdateWrapper<>();
                uw.eq(Goods::getId,goodsId);
                uw.ge(Goods::getStock,cart.getNumber());
                uw.setSql("stock = stock - " + cart.getNumber());

                int row = goodsMapper.update(null,uw);
                if(row==0){
                    throw new StockInsufficientException(goods.getName()+"库存不足");
                }
            }

            //清空购物车
            shoppingCartMapper.delete(qw);

            //订单取消的延时消息丢MQ
            rabbitTemplate.convertAndSend("fresh.order.delay.direct","delay",orders.getNumber(),msg->{
                //spring-amqp 3.x 移除了 setDelay(int)，改用 setDelayLong
                msg.getMessageProperties().setDelayLong(900000L);
                return msg;
            });

        }

        //insert 后 orders 已带自增 id，number 在方法开头生成，一并返回给前端
        return orders;
    }

    @Transactional
    public int paySuccess(String orderNumber){
        //用redisson锁，防误入、自动续期、支持阻塞等待、可重入。
        //即使锁释放与事务提交间存在极小窗口，下方条件更新仍会兜底拒绝重复支付
        RLock lock = redissonClient.getLock(RedisConstant.PAYMENT_LOCK_KEY + orderNumber);
        if(!lock.tryLock()){
            throw new BaseException("请勿重复支付");
        }
        try{
            //只有待支付的订单可以转变订单状态，防止用户支付了取消的订单，乐观锁防重复支付
            LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
            uw.eq(Orders::getNumber,orderNumber);
            uw.eq(Orders::getStatus, Orders.PENDING_PAYMENT);
            uw.set(Orders::getCheckoutTime,LocalDateTime.now());
            uw.set(Orders::getPayStatus, Orders.PAY_STATUS_PAID);
            uw.set(Orders::getStatus, Orders.TO_BE_CONFIRMED);

            int rows = ordersMapper.update(null,uw);
            if(rows==0){
                throw new OrderStatusException("订单已取消或已支付");
            }
            log.info("支付成功");

            LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
            qw.eq(Orders::getNumber,orderNumber);
            Orders orders = ordersMapper.selectOne(qw);

            //通知商家来单
            notifyMerchant(orders.getId(), orderNumber);

            return rows;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 通过 WebSocket 向商家端推送来单提醒（消息类型 type=1）
     * @param orderId 订单id
     * @param orderNumber 订单号
     */
    private void notifyMerchant(Long orderId, String orderNumber) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);//消息类型，1表示来单提醒，2表示客户催单
        map.put("orderId", orderId);
        map.put("content", "订单号：" + orderNumber);

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 查询订单详情（订单基本信息 + 订单明细，仅限当前登录用户自己的订单）
     * @param id 订单id
     * @return 订单详情 VO
     */
    public OrderVO getOrderDetail(Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 查询订单详情：{}", userId, id);

        Orders orders = ordersMapper.selectById(id);
        //不存在或不属于当前用户都按不存在处理，避免越权探测
        if (orders == null || !userId.equals(orders.getUserId())) {
            throw new BaseException("订单不存在");
        }

        //订单基本信息拷贝到 VO（userId、addressBookId 等内部字段不暴露）
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        //查询该订单下的全部明细
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        Integer pageNum = ordersPageQueryDTO.getPage();
        Integer pageSize = ordersPageQueryDTO.getPageSize();
        String number = ordersPageQueryDTO.getNumber();
        String phone = ordersPageQueryDTO.getPhone();
        Integer status = ordersPageQueryDTO.getStatus();
        LocalDateTime beginTime = ordersPageQueryDTO.getBeginTime();
        LocalDateTime endTime = ordersPageQueryDTO.getEndTime();


        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(number)){
            qw.like(Orders::getNumber,number);
        }
        if(StringUtils.isNotBlank(phone)){
            qw.like(Orders::getPhone,phone);
        }
        if(status != null){
            qw.like(Orders::getStatus,status);
        }
        if(beginTime != null){
            qw.ge(Orders::getOrderTime,beginTime);
        }
        if(endTime != null){
            qw.le(Orders::getOrderTime,endTime);
        }
        //下单时间倒序（最近的订单在最前面），同一时间下单的再按订单id倒序
        qw.orderByDesc(Orders::getOrderTime);
        qw.orderByDesc(Orders::getId);

        Page<Orders> list =(Page<Orders>) ordersMapper.selectList(qw);

        PageResult pageResult = new PageResult();
        pageResult.setRecords(list);
        pageResult.setTotal(list.getTotal());

        return pageResult;
    }

}
