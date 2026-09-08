package com.fresh.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 对话回答：最终自然语言答案 + 本轮工具调用轨迹
 */
@Data
public class AiChatVO implements Serializable {

    /**
     * AI 的最终自然语言回答
     */
    private String answer;

    /**
     * AI 本轮实际调用的工具轨迹（供前端展示"AI 查了什么、查到什么"）
     */
    private List<ToolTraceVO> toolTrace;

}
