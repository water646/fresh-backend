package com.fresh.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * AI 对话请求体：对应 /v1/messages 接口的 JSON 结构，
 * 由后端组装后通过 HttpClientUtil 发给 AI 服务（智谱 Anthropic 兼容端点）
 */
@Data
public class AiChatRequest {

    /** 模型名称，如 glm-5.3 */
    private String model;

    /** 单次回答的最大 token 数（协议必填字段，蛇形命名 max_tokens） */
    @JSONField(name = "max_tokens")
    private Long maxTokens;

    /** system 提示词（角色设定 + 当天日期等） */
    private String system;

    /** 对话消息列表，按 user / assistant 交替 */
    private List<AiMessage> messages;

    /** 可供模型调用的工具定义（白名单） */
    private List<AiToolDef> tools;

}
