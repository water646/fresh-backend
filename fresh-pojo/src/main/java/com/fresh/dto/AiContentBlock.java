package com.fresh.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

/**
 * AI 消息内容块：一条消息由若干内容块组成，同一对象承载三种块，
 * 靠 type 区分，未用到的字段序列化时不输出（fastjson 默认不序列化 null 值）
 * <pre>
 * text       —— 普通文本（text 有值）
 * tool_use   —— 模型发起的工具调用（id / name / input 有值）
 * tool_result—— 后端回传的工具执行结果（toolUseId / content 有值）
 * </pre>
 */
@Data
public class AiContentBlock {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_TOOL_USE = "tool_use";
    public static final String TYPE_TOOL_RESULT = "tool_result";

    /** 块类型：text / tool_use / tool_result */
    private String type;

    /** 文本内容（text 块） */
    private String text;

    /** 本次工具调用的唯一标识，用于配对结果（tool_use 块） */
    private String id;

    /** 要调用的工具名（tool_use 块） */
    private String name;

    /** 模型给出的工具入参（tool_use 块） */
    private Map<String, Object> input;

    /** 对应 tool_use 块的 id（tool_result 块，蛇形命名 tool_use_id） */
    @JSONField(name = "tool_use_id")
    private String toolUseId;

    /** 工具执行结果，这里放 JSON 字符串（tool_result 块） */
    private String content;

    /** 构造文本块 */
    public static AiContentBlock textOf(String text) {
        AiContentBlock block = new AiContentBlock();
        block.setType(TYPE_TEXT);
        block.setText(text);
        return block;
    }

    /** 构造工具结果块 */
    public static AiContentBlock toolResultOf(String toolUseId, String content) {
        AiContentBlock block = new AiContentBlock();
        block.setType(TYPE_TOOL_RESULT);
        block.setToolUseId(toolUseId);
        block.setContent(content);
        return block;
    }

}
