package com.fresh.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * AI 工具定义：随请求发给模型，告诉模型有哪些工具可调、怎么传参，
 * 构成模型可执行命令的白名单
 */
@Data
public class AiToolDef {

    /** 工具名，模型调用时按此名字分发 */
    private String name;

    /** 工具功能说明，模型据此决定是否调用 */
    private String description;

    /** 入参 JSON Schema（蛇形命名 input_schema） */
    @JSONField(name = "input_schema")
    private AiToolSchema inputSchema;

}
