package com.fresh.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fresh.dto.AiToolDef;
import com.fresh.dto.AiToolSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 工具注册中心：收集所有 AiTool 实现，
 * 对上导出给模型看的工具定义（白名单），对下按工具名分发执行
 */
@Component
@Slf4j
public class ToolRegistry {

    private final Map<String, AiTool> tools = new LinkedHashMap<>();

    public ToolRegistry(List<AiTool> toolBeans) {
        for (AiTool tool : toolBeans) {
            tools.put(tool.name(), tool);
        }
        log.info("AI工具注册完成：{}", tools.keySet());
    }

    /**
     * 导出全部工具定义，随请求发给模型，构成可调用命令的白名单
     */
    public List<AiToolDef> toolDefinitions() {
        List<AiToolDef> list = new ArrayList<>();
        for (AiTool tool : tools.values()) {
            AiToolSchema schema = new AiToolSchema();
            schema.setProperties(tool.inputSchema());
            schema.setRequired(tool.requiredParams());

            AiToolDef def = new AiToolDef();
            def.setName(tool.name());
            def.setDescription(tool.description());
            def.setInputSchema(schema);
            list.add(def);
        }
        return list;
    }

    /**
     * 按工具名分发执行（白名单之外的命令一律拒绝），
     * 结果序列化为 JSON 字符串回传给模型，让模型基于事实组织回答
     */
    public String execute(String name, Map<String, Object> input) {
        AiTool tool = tools.get(name);
        if (tool == null) {
            log.warn("AI调用了未注册的工具：{}", name);
            return errorJson("未知工具：" + name);
        }
        try {
            Map<String, Object> args = input == null ? Collections.emptyMap() : input;
            long start = System.currentTimeMillis();
            Object result = tool.execute(args);
            log.info("AI工具 {} 执行完成，耗时 {} ms，入参：{}", name, System.currentTimeMillis() - start, args);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            //执行失败也以 JSON 回传，让模型自行纠正参数或向用户说明
            log.warn("AI工具 {} 执行失败：{}", name, e.getMessage());
            return errorJson(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    /**
     * 构造错误回传 JSON
     */
    private String errorJson(String message) {
        JSONObject error = new JSONObject();
        error.put("error", message);
        return error.toJSONString();
    }

}
