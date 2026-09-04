package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.constant.JwtClaimsConstant;
import com.fresh.constant.MessageConstant;
import com.fresh.constant.StatusConstant;
import com.fresh.context.BaseContext;
import com.fresh.dto.EmployeeAddDTO;
import com.fresh.dto.EmployeeLoginDTO;
import com.fresh.dto.EmployeePageQueryDTO;
import com.fresh.dto.EmployeeUpdateDTO;
import com.fresh.entity.Employee;
import com.fresh.exception.AccountLockedException;
import com.fresh.exception.AccountNotFoundException;
import com.fresh.exception.BaseException;
import com.fresh.exception.PasswordErrorException;
import com.fresh.mapper.EmployeeMapper;
import com.fresh.properties.JwtProperties;
import com.fresh.result.PageResult;
import com.fresh.service.EmployeeService;
import com.fresh.utils.JwtUtil;
import com.fresh.vo.EmployeeLoginVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 员工登录
     * @param employeeLoginDTO 员工登录时传递的数据
     * @return 员工登录后返回的数据
     */
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //根据用户名查数据库（username 有唯一索引，selectOne 安全）
        Employee employee = employeeMapper.selectOne(
                new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, username));

        //账号不存在
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对（数据库存的是 MD5 值）
        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //账号被锁定
        if (StatusConstant.DISABLE.equals(employee.getStatus())) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        return EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
    }

    /**
     * 新增员工
     * @param employeeAddDTO 新增员工时传递的数据
     */
    public void save(EmployeeAddDTO employeeAddDTO) {
        log.info("新增员工：{}", employeeAddDTO);

        //必填校验（employee 表除备注外字段均 NOT NULL，缺失时给出明确提示而不是数据库报错）
        checkRequired("姓名", employeeAddDTO.getName());
        checkRequired("用户名", employeeAddDTO.getUsername());
        checkRequired("密码", employeeAddDTO.getPassword());
        checkRequired("手机号", employeeAddDTO.getPhone());
        checkRequired("性别", employeeAddDTO.getSex());
        checkRequired("身份证号", employeeAddDTO.getIdNumber());

        //用户名唯一校验（username 有唯一索引，重复插入会直接报错，这里提前给出友好提示）
        Long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, employeeAddDTO.getUsername()));
        if (count > 0) {
            throw new BaseException(employeeAddDTO.getUsername() + MessageConstant.ALREADY_EXISTS);
        }

        Employee employee = new Employee();
        //属性拷贝（字段名和类型一致：name/username/phone/sex/idNumber）
        BeanUtils.copyProperties(employeeAddDTO, employee);
        //密码加密存储（数据库存 MD5 值，与登录时的比对逻辑一致）
        employee.setPassword(DigestUtils.md5DigestAsHex(employeeAddDTO.getPassword().getBytes()));
        //新增员工默认启用
        employee.setStatus(StatusConstant.ENABLE);
        //createTime 等公共字段由 AutoFillAspect 在 insert 前自动填充
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询员工（支持姓名模糊查询，按 id 倒序）
     * @param employeePageQueryDTO 分页查询条件
     * @return 分页结果
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        String name = employeePageQueryDTO.getName();
        Integer pageNum = employeePageQueryDTO.getPageNum();
        Integer pageSize = employeePageQueryDTO.getPageSize();

        //设置分页参数；紧跟其后的第一条 SQL 会被追加分页
        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            qw.like(Employee::getName, name);
        }
        qw.orderByDesc(Employee::getId);

        Page<Employee> page = (Page<Employee>) employeeMapper.selectList(qw);

        //密码不外露，返回前清空
        page.getResult().forEach(e -> e.setPassword(null));

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询员工
     * @param id 员工id
     * @return 员工实体（不存在时为 null，password 不返回）
     */
    public Employee getById(Long id) {
        log.info("根据id查询员工：{}", id);
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            return null;
        }
        //密码不外露，返回前清空
        employee.setPassword(null);
        return employee;
    }

    /**
     * 修改员工（部分更新：只更新传了的字段；启用/禁用也走本接口）
     * @param employeeUpdateDTO 修改员工时传递的数据
     */
    public void update(EmployeeUpdateDTO employeeUpdateDTO) {
        Long currentId = BaseContext.getCurrentId();
        log.info("员工 {} 修改员工：{}", currentId, employeeUpdateDTO);

        if (employeeUpdateDTO.getId() == null) {
            throw new BaseException("员工id不能为空");
        }

        //禁用自己会导致当前账号下次无法登录，禁止
        if (employeeUpdateDTO.getId().equals(currentId)
                && StatusConstant.DISABLE.equals(employeeUpdateDTO.getStatus())) {
            throw new BaseException("不能禁用当前登录的账号");
        }

        Employee employee = new Employee();
        //属性拷贝（name/phone/sex/idNumber/status；DTO 中无 username/password，不会被覆盖）
        BeanUtils.copyProperties(employeeUpdateDTO, employee);
        //updateTime/updateUser 由 AutoFillAspect 在更新前自动填充；
        //MP 的 updateById 默认忽略 null 字段，前端没传的字段不会被覆盖
        employeeMapper.updateById(employee);
    }

    /**
     * 根据id删除员工（不能删除当前登录员工自己）
     * @param id 员工id
     */
    public void delete(Long id) {
        Long currentId = BaseContext.getCurrentId();
        log.info("员工 {} 删除员工：{}", currentId, id);

        //删除自己会导致当前账号立即失效，禁止
        if (id != null && id.equals(currentId)) {
            throw new BaseException("不能删除当前登录的账号");
        }
        employeeMapper.deleteById(id);
    }

    /**
     * 必填字段校验，为空时抛出业务异常
     * @param field 中文名称，用于拼接提示信息
     * @param value 字段值
     */
    private void checkRequired(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BaseException(field + "不能为空");
        }
    }
}
