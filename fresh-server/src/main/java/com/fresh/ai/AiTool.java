package com.fresh.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 工具接口：一个实现类代表一个可供模型调用的"命令"（function calling）
 * 实现类加 @Component 后由 ToolRegistry 自动收集注册，构成可调用命令的白名单
 */
public interface AiTool {

    /**
     * 工具名（模型调用时引用，蛇形命名，如 query_goods_sales）
     */
    String name();

    /**
     * 工具说明（给模型看：什么问题该用这个工具、统计口径是什么）
     */
    String description();

    /**
     * 参数 JSON Schema：参数名 -> 该参数的 schema 片段（type/description 等）
     */
    Map<String, Object> inputSchema();

    /**
     * 必填参数名列表（无必填参数返回空列表）
     */
    List<String> requiredParams();

    /**
     * 执行工具（只做查询，不做写操作），返回可序列化为 JSON 的结果
     * @param args 模型按 schema 填写的参数
     */
    Object execute(Map<String, Object> args);

}
