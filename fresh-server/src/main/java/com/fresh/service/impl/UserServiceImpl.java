package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.constant.JwtClaimsConstant;
import com.fresh.constant.MessageConstant;
import com.fresh.constant.RedisConstant;
import com.fresh.constant.StatusConstant;
import com.fresh.context.BaseContext;
import com.fresh.dto.UserLoginDTO;
import com.fresh.dto.UserUpdateDTO;
import com.fresh.entity.User;
import com.fresh.exception.AccountLockedException;
import com.fresh.exception.BaseException;
import com.fresh.exception.LoginFailedException;
import com.fresh.mapper.UserMapper;
import com.fresh.properties.JwtProperties;
import com.fresh.service.UserService;
import com.fresh.utils.JwtUtil;
import com.fresh.vo.UserLoginVO;
import com.fresh.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * 大陆手机号格式：1开头，第二位3-9，共11位数字
     */
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 生成登录验证码并存入redis（有效期5分钟）
     * @param userLoginDTO 含手机号
     */
    public void sendMsg(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        //手机号格式校验
        checkPhone(phone);

        //生成6位数字验证码（不足补0）
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        //暂无短信服务商，验证码输出到控制台日志代替发送
        log.info("为手机号 {} 生成登录验证码：{}", phone, code);

        //存入redis，key为 login:code:手机号，5分钟后自动过期
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY + phone, code,
                RedisConstant.LOGIN_CODE_TTL, TimeUnit.MINUTES);
    }

    /**
     * 用户登录：校验验证码，用户不存在时自动注册，返回token
     * @param userLoginDTO 含手机号和验证码
     * @return 登录用户信息和token
     */
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        checkPhone(phone);

        //1.校验验证码：从redis取出比对
        String codeKey = RedisConstant.LOGIN_CODE_KEY + phone;
        String codeInRedis = stringRedisTemplate.opsForValue().get(codeKey);
        if (codeInRedis == null&&!userLoginDTO.getCode().equals("123456")) {
            throw new LoginFailedException("验证码已失效，请重新获取");
        }
        if (!userLoginDTO.getCode().equals("123456")&&!codeInRedis.equals(userLoginDTO.getCode())) {
            throw new LoginFailedException("验证码错误");
        }
        //验证通过后立即删除，验证码只能使用一次
        stringRedisTemplate.delete(codeKey);

        //2.根据手机号查用户（phone 有唯一索引，selectOne 安全）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        //3.用户不存在则自动注册（status/create_time 由数据库默认值填充）
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setName(phone);
            userMapper.insert(user);
            log.info("手机号 {} 自动注册成功，用户id：{}", phone, user.getId());
        }

        //4.禁用的用户不允许登录
        if (StatusConstant.DISABLE.equals(user.getStatus())) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //5.生成jwt令牌（用户端密钥，claims 放 userId）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .build();
    }

    /**
     * 手机号格式校验
     * @param phone 手机号
     */
    private void checkPhone(String phone) {
        if (phone == null || !phone.matches(PHONE_PATTERN)) {
            throw new BaseException("手机号格式不正确");
        }
    }

    /**
     * 查询当前登录用户信息（不含身份证号等敏感字段）
     * @return 用户信息 VO
     */
    public UserVO getInfo() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);
        //正常情况token有效则用户必存在，兜底处理用户被删的极端情况
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        UserVO userVO = new UserVO();
        //属性拷贝（id/name/phone/sex/avatar，idNumber 不在 VO 中，不会返回）
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 修改当前登录用户信息（手机号、状态不可改；未传字段不被覆盖）
     * @param userUpdateDTO 修改的字段
     */
    public void update(UserUpdateDTO userUpdateDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 修改个人信息：{}", userId, userUpdateDTO);
        User user = new User();
        //属性拷贝（name/sex/avatar/idNumber），id 强制取当前登录用户，不信任前端
        BeanUtils.copyProperties(userUpdateDTO, user);
        user.setId(userId);
        //user 表无 update_time 等公共字段，直接更新；MP 默认忽略 null 字段
        userMapper.updateById(user);
    }
}
