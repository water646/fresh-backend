package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询员工时传递的数据
 */
@Data
@Schema(name = "EmployeePageQueryDTO", description = "分页查询员工时传递的数据")
public class EmployeePageQueryDTO implements Serializable {

    /**
     * 页码
     */
    @Schema(description = "页码", required = false, example = "1")
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", required = false, example = "10")
    private Integer pageSize = 10;

    /**
     * 员工姓名模糊查询
     */
    @Schema(description = "员工姓名模糊查询")
    private String name;
}
