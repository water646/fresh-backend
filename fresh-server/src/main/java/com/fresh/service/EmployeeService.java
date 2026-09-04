package com.fresh.service;

import com.fresh.dto.EmployeeAddDTO;
import com.fresh.dto.EmployeeLoginDTO;
import com.fresh.dto.EmployeePageQueryDTO;
import com.fresh.dto.EmployeeUpdateDTO;
import com.fresh.entity.Employee;
import com.fresh.result.PageResult;
import com.fresh.vo.EmployeeLoginVO;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO 员工登录时传递的数据
     * @return 员工登录后返回的数据
     */
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeAddDTO 新增员工时传递的数据
     */
    void save(EmployeeAddDTO employeeAddDTO);

    /**
     * 分页查询员工（支持姓名模糊查询）
     * @param employeePageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据id查询员工
     * @param id 员工id
     * @return 员工实体（不存在时为 null，password 不返回）
     */
    Employee getById(Long id);

    /**
     * 修改员工（部分更新：只更新传了的字段；启用/禁用也走本接口）
     * @param employeeUpdateDTO 修改员工时传递的数据
     */
    void update(EmployeeUpdateDTO employeeUpdateDTO);

    /**
     * 根据id删除员工（不能删除当前登录员工自己）
     * @param id 员工id
     */
    void delete(Long id);
}
