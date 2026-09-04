package com.fresh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddressBookDTO {
    /**
     * 收货人
     */
    @ApiModelProperty(value = "收货人")
    private String consignee;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 性别 0女 1男
     */
    @ApiModelProperty(value = "性别 0女 1男")
    private String sex;

    /**
     * 详细地址
     */
    @ApiModelProperty(value = "详细地址")
    private String detail;

    /**
     * 标签（如"家"、"公司"、"学校"）
     */
    @ApiModelProperty(value = "标签")
    private String label;

}
