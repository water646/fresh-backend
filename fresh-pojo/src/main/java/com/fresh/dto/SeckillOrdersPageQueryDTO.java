package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀订单分页查询时传递的数据
 */
@Data
@ApiModel(value = "SeckillOrdersPageQueryDTO", description = "秒杀订单分页查询时传递的数据")
public class SeckillOrdersPageQueryDTO implements Serializable {

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    private Integer page;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    private Integer pageSize;

    /**
     * 订单号（模糊查询）
     */
    @ApiModelProperty(value = "订单号（模糊查询）")
    private String number;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @ApiModelProperty(value = "订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消")
    private Integer status;

    /**
     * 手机号（模糊查询）
     */
    @ApiModelProperty(value = "手机号（模糊查询）")
    private String phone;

    /**
     * 下单时间范围-开始
     */
    @ApiModelProperty(value = "下单时间范围-开始，格式 yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 下单时间范围-结束
     */
    @ApiModelProperty(value = "下单时间范围-结束，格式 yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
