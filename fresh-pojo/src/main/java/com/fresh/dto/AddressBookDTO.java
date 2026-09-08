package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class AddressBookDTO {
    /**
     * 收货人
     */
    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人长度不能超过50")
    @Schema(description = "收货人")
    private String consignee;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别 0女 1男
     */
    @Pattern(regexp = "^[01]$", message = "性别只能为0或1")
    @Schema(description = "性别 0女 1男")
    private String sex;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过255")
    @Schema(description = "详细地址")
    private String detail;

    /**
     * 标签（如"家"、"公司"、"学校"）
     */
    @Size(max = 50, message = "标签长度不能超过50")
    @Schema(description = "标签")
    private String label;

}
