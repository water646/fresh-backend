package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.fresh.annotation.AutoFill;
import com.fresh.entity.Employee;
import com.fresh.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 员工 Mapper：继承 MyBatis-Plus 的 BaseMapper，自带单表 CRUD
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 插入一条员工数据
     * 重新声明 BaseMapper 的 insert 并标注 @AutoFill，
     * 让 AutoFillAspect 在插入前填充 createTime/createUser 等公共字段
     */
    @Override
    @AutoFill(OperationType.INSERT)
    int insert(Employee employee);

    /**
     * 根据 id 修改员工数据
     * 同样重写声明并标注 @AutoFill，让切面在更新前填充 updateTime/updateUser；
     * 注意必须保留 @Param(Constants.ENTITY)（即 "et"），MP 注入的 update SQL 通过 #{et.字段} 取值
     */
    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param(Constants.ENTITY) Employee employee);
}
