package com.fresh.service;

import com.fresh.dto.UserLoginDTO;
import com.fresh.dto.UserUpdateDTO;
import com.fresh.vo.UserLoginVO;
import com.fresh.vo.UserVO;

public interface UserService {

    /**
     * 生成登录验证码并存入redis（有效期5分钟）
     * @param userLoginDTO 含手机号
     */
    void sendMsg(UserLoginDTO userLoginDTO);

    /**
     * 用户登录：校验验证码，用户不存在时自动注册，返回token
     * @param userLoginDTO 含手机号和验证码
     * @return 登录用户信息和token
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 查询当前登录用户信息（不含身份证号等敏感字段）
     * @return 用户信息 VO
     */
    UserVO getInfo();

    /**
     * 修改当前登录用户信息（手机号、状态不可改；未传字段不被覆盖）
     * @param userUpdateDTO 修改的字段
     */
    void update(UserUpdateDTO userUpdateDTO);
}
