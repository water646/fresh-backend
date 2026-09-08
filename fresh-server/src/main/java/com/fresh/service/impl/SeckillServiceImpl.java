package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.constant.RedisConstant;
import com.fresh.context.BaseContext;
import com.fresh.config.RabbitMqConfig;
import com.fresh.dto.SeckillGoodsAddDTO;
import com.fresh.dto.SeckillGoodsPageQueryDTO;
import com.fresh.dto.SeckillGoodsUpdateDTO;
import com.fresh.entity.SeckillGoods;
import com.fresh.entity.SeckillOrders;
import com.fresh.exception.GoodsNotExistsException;
import com.fresh.mapper.SeckillMapper;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.SeckillService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, SeckillGoods> implements SeckillService {

    @Autowired
    private SeckillMapper seckillMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    //在static代码块加载lua脚本，返回值是Long
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    @Override
    public void add(SeckillGoodsAddDTO seckillGoodsAddDTO){
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtils.copyProperties(seckillGoodsAddDTO,seckillGoods);
        seckillGoods.setStatus(0);
        seckillMapper.insert(seckillGoods);
    }

    @Override
    public void update(SeckillGoodsUpdateDTO seckillGoodsUpdateDTO){
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtils.copyProperties(seckillGoodsUpdateDTO,seckillGoods);
        seckillMapper.updateById(seckillGoods);

        //在此时预热缓存（key格式与seckill.lua中读取的保持一致：fresh:seckill:stock:商品id）
        //stock 取更新后数据库的最新值：本次没传 stock 时也不会把缓存写成 "null" 导致秒杀一直提示库存不足
        SeckillGoods dbGoods = seckillMapper.selectById(seckillGoodsUpdateDTO.getId());
        if (dbGoods != null) {
            stringRedisTemplate.opsForValue().set(RedisConstant.SECKILL_STOCK_KEY + dbGoods.getId(),
                    String.valueOf(dbGoods.getStock()));
        }
    }

    @Override
    public PageResult pageQuery(SeckillGoodsPageQueryDTO dto) {

        // 开启分页
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 构造查询条件
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(dto.getName() != null && !dto.getName().isEmpty(),
                        SeckillGoods::getName, dto.getName())
                .eq(dto.getStatus() != null,
                        SeckillGoods::getStatus, dto.getStatus())
                .orderByDesc(SeckillGoods::getCreateTime);

        // 查询
        List<SeckillGoods> list = seckillMapper.selectList(wrapper);

        // PageHelper 会将 List 转换成 Page
        Page<SeckillGoods> page = (Page<SeckillGoods>) list;

        return new PageResult(
                page.getTotal(),
                page.getResult()
        );
    }

    @Override
    public Map<String,Object> seckill(Long seckillGoodsId) {

        Long userId = BaseContext.getCurrentId();

        //1.判断商品存在
        SeckillGoods seckillGoods = getById(seckillGoodsId);
        if (seckillGoods == null) {
            throw new GoodsNotExistsException("商品不存在");
        }

        //2.判断商品已启用（status 0禁用 1启用，禁用的秒杀商品不允许下单）
        if (seckillGoods.getStatus() == null || seckillGoods.getStatus() != 1) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("msg","秒杀商品已禁用");
            return map;
        }

        //3.判断在秒杀时间内
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(seckillGoods.getStartTime())||now.isAfter(seckillGoods.getEndTime())){
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("msg","不在秒杀时间内");
            return map;
        }


        //4.lua脚本执行库存扣减和确保一人一单（StringRedisTemplate 的参数序列化只支持字符串，Long 会强转失败）
        Long buyResult = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(),
                seckillGoodsId.toString(), userId.toString());
        //判断结果是否为0
        int r = buyResult.intValue();

        //不为0，代表没有购买资格
        if (r != 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("msg",r==1?"库存不足":"已经购买过了");
            return map;
        }

        //5.创建订单
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RedisConstant.ORDER_SEQUENCE_KEY + date;

        Long sequence = stringRedisTemplate.opsForValue().increment(key);
        String orderNumber = date + String.format("%08d", sequence);

        SeckillOrders order = new SeckillOrders();
        order.setUserId(userId);
        order.setSeckillGoodsId(seckillGoodsId);
        order.setNumber(orderNumber);
        order.setStatus(SeckillOrders.PENDING_PAYMENT);
        order.setOrderTime(LocalDateTime.now());
        order.setAmount(seckillGoods.getSeckillPrice());

        //6.MQ异步写数据库：订单发到RabbitMQ，由SeckillOrderListener消费落库（削峰，秒杀流量不直接打数据库）
        rabbitTemplate.convertAndSend(RabbitMqConfig.SECKILL_EXCHANGE, RabbitMqConfig.SECKILL_ROUTING_KEY, order);

        //7.MQ清理超时订单，现在投放延时消息
        rabbitTemplate.convertAndSend("fresh.order.delay.direct","delay",order.getNumber(),msg->{
            //spring-amqp 3.x 移除了 setDelay(int)，改用 setDelayLong
            msg.getMessageProperties().setDelayLong(900000L);
            return msg;
        });

        Map<String, Object> map = new HashMap<>();
        map.put("code", 1);
        map.put("msg","success");

        return map;
    }

}
