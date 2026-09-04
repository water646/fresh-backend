package com.fresh.controller.admin;

import com.fresh.dto.EmployeeAddDTO;
import com.fresh.dto.EmployeeLoginDTO;
import com.fresh.dto.EmployeePageQueryDTO;
import com.fresh.dto.EmployeeUpdateDTO;
import com.fresh.entity.Employee;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.EmployeeService;
import com.fresh.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Api(tags = "员工相关接口")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param employeeLoginDTO 员工登录时传递的数据
     * @return 员工登录后返回的数据
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO.getUsername());
        EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);
        return Result.success(employeeLoginVO);
    }

    /**
     * 新增员工
     * @param employeeAddDTO 新增员工时传递的数据
     * @return 成功标识
     */
    @PostMapping("/add")
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeAddDTO employeeAddDTO) {
        log.info("新增员工：{}", employeeAddDTO);
        employeeService.save(employeeAddDTO);
        return Result.success();
    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("分页查询员工：{}", employeePageQueryDTO);
        return Result.success(employeeService.pageQuery(employeePageQueryDTO));
    }

    /**
     * 根据id查询员工
     * @param id 员工id
     * @return 员工信息（不存在时 data 为 null，不含密码）
     */
    @GetMapping("/get")
    @ApiOperation("根据id查询员工")
    public Result<Employee> get(Long id) {
        log.info("根据id查询员工：{}", id);
        return Result.success(employeeService.getById(id));
    }

    /**
     * 根据id删除员工
     * @param id 员工id
     * @return 成功标识
     */
    @DeleteMapping("/delete")
    @ApiOperation("根据id删除员工")
    public Result delete(Long id) {
        log.info("根据id删除员工：{}", id);
        employeeService.delete(id);
        return Result.success();
    }

    /**
     * 修改员工（部分更新：只更新传了的字段；启用/禁用也走本接口）
     * @param employeeUpdateDTO 修改员工时传递的数据
     * @return 成功标识
     */
    @PostMapping("/update")
    @ApiOperation("修改员工")
    public Result update(@RequestBody EmployeeUpdateDTO employeeUpdateDTO) {
        log.info("修改员工：{}", employeeUpdateDTO);
        employeeService.update(employeeUpdateDTO);
        return Result.success();
    }
}
