package com.fresh.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 工具入参的 JSON Schema：声明每个参数的类型/说明/枚举值，以及哪些参数必填
 */
@Data
public class AiToolSchema {

    /** 固定为 object */
    private String type = "object";

    /** 每个参数名 → 参数说明（type / description / enum 等） */
    private Map<String, Object> properties;

    /** 必填参数名列表 */
    private List<String> required;

}
