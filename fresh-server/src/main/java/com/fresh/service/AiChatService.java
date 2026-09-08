package com.fresh.service;

import com.fresh.vo.AiChatVO;

/**
 * AI 对话服务：把商家的问题交给大模型，
 * 模型按需调用已注册的工具（ToolRegistry 白名单）查询经营数据后组织回答
 */
public interface AiChatService {

    /**
     * AI 对话
     * @param question 商家的问题
     * @return 最终自然语言回答 + 本轮工具调用轨迹
     */
    AiChatVO chat(String question);

}
