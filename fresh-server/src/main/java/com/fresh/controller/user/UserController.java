package com.fresh.controller.user;

import com.fresh.dto.UserLoginDTO;
import com.fresh.dto.UserUpdateDTO;
import com.fresh.result.Result;
import com.fresh.service.UserService;
import com.fresh.vo.UserLoginVO;
import com.fresh.vo.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C端用户相关接口
 */
@RestController
@RequestMapping("/user/user")
@Tag(name = "C端用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送登录验证码
     * @param userLoginDTO 含手机号
     * @return 成功标识
     */
    @PostMapping("/sendMsg")
    @Operation(summary = "发送登录验证码")
    public Result sendMsg(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("发送登录验证码：{}", userLoginDTO.getPhone());
        userService.sendMsg(userLoginDTO);
        return Result.success();
    }

    /**
     * 用户登录
     * @param userLoginDTO 含手机号和验证码
     * @return 登录用户id和token
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO.getPhone());
        return Result.success(userService.login(userLoginDTO));
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息（不含身份证号）
     */
    @GetMapping("/get")
    @Operation(summary = "获取当前登录用户信息")
    public Result<UserVO> get() {
        log.info("获取当前登录用户信息");
        return Result.success(userService.getInfo());
    }

    /**
     * 修改当前登录用户信息（手机号不可改）
     * @param userUpdateDTO 待修改的字段
     * @return 成功标识
     */
    @PostMapping("/update")
    @Operation(summary = "修改当前登录用户信息")
    public Result update(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("修改当前登录用户信息：{}", userUpdateDTO);
        userService.update(userUpdateDTO);
        return Result.success();
    }
}
