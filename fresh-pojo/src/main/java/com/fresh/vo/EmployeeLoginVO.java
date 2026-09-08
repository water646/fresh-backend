package com.fresh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录后返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EmployeeLoginVO", description = "员工登录后返回的数据")
public class EmployeeLoginVO implements Serializable {

    /**
     * 员工id
     */
    @Schema(description = "员工id")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * jwt令牌
     */
    @Schema(description = "jwt令牌")
    private String token;
}
