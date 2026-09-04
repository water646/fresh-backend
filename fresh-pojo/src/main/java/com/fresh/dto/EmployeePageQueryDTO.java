package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询员工时传递的数据
 */
@Data
@ApiModel(value = "EmployeePageQueryDTO", description = "分页查询员工时传递的数据")
public class EmployeePageQueryDTO implements Serializable {

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码", required = false, example = "1")
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    @ApiModelProperty(value = "每页记录数", required = false, example = "10")
    private Integer pageSize = 10;

    /**
     * 员工姓名模糊查询
     */
    @ApiModelProperty(value = "员工姓名模糊查询")
    private String name;
}
