package com.fresh.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 对话中的一条消息（user 或 assistant），
 * content 统一用内容块列表表示：文本是 text 块，工具调用是 tool_use / tool_result 块
 */
@Data
public class AiMessage {

    /** 消息角色：user / assistant */
    private String role;

    /** 消息内容块列表（文本、工具调用、工具结果都算内容块） */
    private List<AiContentBlock> content;

    public static AiMessage of(String role, List<AiContentBlock> content) {
        AiMessage message = new AiMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

}
