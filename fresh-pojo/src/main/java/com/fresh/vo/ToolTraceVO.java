package com.fresh.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 单次工具调用轨迹：工具名 + AI 填写的入参 + 工具返回的结果
 */
@Data
public class ToolTraceVO implements Serializable {

    /**
     * 工具名，如 query_goods_sales
     */
    private String toolName;

    /**
     * AI 填写的入参（JSON 字符串）
     */
    private String arguments;

    /**
     * 工具执行返回的结果（JSON 字符串）
     */
    private String result;

}
