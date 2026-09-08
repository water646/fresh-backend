package com.fresh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀订单分页查询时传递的数据
 */
@Data
@Schema(name = "SeckillOrdersPageQueryDTO", description = "秒杀订单分页查询时传递的数据")
public class SeckillOrdersPageQueryDTO implements Serializable {

    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer page;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /**
     * 订单号（模糊查询）
     */
    @Schema(description = "订单号（模糊查询）")
    private String number;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @Schema(description = "订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消")
    private Integer status;

    /**
     * 手机号（模糊查询）
     */
    @Schema(description = "手机号（模糊查询）")
    private String phone;

    /**
     * 下单时间范围-开始
     */
    @Schema(description = "下单时间范围-开始，格式 yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 下单时间范围-结束
     */
    @Schema(description = "下单时间范围-结束，格式 yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
