package com.fresh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fresh.ai.ToolRegistry;
import com.fresh.dto.AiChatRequest;
import com.fresh.dto.AiContentBlock;
import com.fresh.dto.AiMessage;
import com.fresh.dto.AiToolDef;
import com.fresh.exception.BaseException;
import com.fresh.properties.AiProperties;
import com.fresh.service.AiChatService;
import com.fresh.utils.HttpClientUtil;
import com.fresh.vo.AiChatVO;
import com.fresh.vo.ToolTraceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI 对话服务实现：function calling 主循环
 * 问题 + 工具定义通过 HttpClientUtil 发给 AI 服务 → 模型返回 tool_use →
 * ToolRegistry 白名单执行 → 结果作为 tool_result 回传 → 循环直到模型给出最终自然语言回答
 */
@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    /** 工具调用轮数上限，防止模型反复调工具停不下来 */
    private static final int MAX_TOOL_ROUNDS = 5;
    /** 单次 HTTP 请求超时（毫秒）：大模型生成回答较慢，默认 5 秒远远不够 */
    private static final int HTTP_TIMEOUT_MSEC = 60 * 1000;
    /** Anthropic 协议要求的版本号请求头 */
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter WEEK = DateTimeFormatter.ofPattern("E", Locale.CHINESE);

    @Autowired
    private AiProperties aiProperties;
    @Autowired
    private ToolRegistry toolRegistry;

    @Override
    public AiChatVO chat(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new BaseException("问题不能为空");
        }

        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.of("user",
                Collections.singletonList(AiContentBlock.textOf(question.trim()))));
        List<ToolTraceVO> toolTrace = new ArrayList<>();

        try {
            for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                JSONObject resp = callAi(messages);
                JSONArray content = resp.getJSONArray("content");
                if (content == null || content.isEmpty()) {
                    throw new BaseException("AI 未返回有效回答，请重试");
                }

                //响应内容块逐个取出：全部回填为 assistant 消息（模型下一轮靠 tool_use 的 id 配对结果），
                //其中的 tool_use 块就地执行，结果攒成 tool_result 块
                List<AiContentBlock> assistantBlocks = new ArrayList<>();
                List<AiContentBlock> toolResults = new ArrayList<>();
                for (int i = 0; i < content.size(); i++) {
                    JSONObject jsonBlock = content.getJSONObject(i);
                    AiContentBlock block = new AiContentBlock();
                    block.setType(jsonBlock.getString("type"));
                    block.setText(jsonBlock.getString("text"));
                    block.setId(jsonBlock.getString("id"));
                    block.setName(jsonBlock.getString("name"));
                    block.setInput(jsonBlock.getJSONObject("input"));
                    assistantBlocks.add(block);

                    if (AiContentBlock.TYPE_TOOL_USE.equals(block.getType())) {
                        String resultJson = toolRegistry.execute(block.getName(), block.getInput());
                        toolResults.add(AiContentBlock.toolResultOf(block.getId(), resultJson));
                        toolTrace.add(trace(block.getName(), block.getInput(), resultJson));
                    }
                }
                messages.add(AiMessage.of("assistant", assistantBlocks));

                if (AiContentBlock.TYPE_TOOL_USE.equals(resp.getString("stop_reason"))) {
                    //本轮模型可能并行请求多个工具：全部执行后，作为一个 user 消息整体回传
                    if (toolResults.isEmpty()) {
                        throw new BaseException("AI 请求了无法识别的操作");
                    }
                    messages.add(AiMessage.of("user", toolResults));
                    continue;
                }

                //end_turn：拼接最终文本回答
                StringBuilder answer = new StringBuilder();
                for (AiContentBlock block : assistantBlocks) {
                    if (block.getText() != null) {
                        answer.append(block.getText());
                    }
                }
                if (answer.length() == 0) {
                    throw new BaseException("AI 未返回有效回答，请重试");
                }
                log.info("AI回答完成，共调用工具 {} 次", toolTrace.size());
                AiChatVO aiChatVO = new AiChatVO();
                aiChatVO.setAnswer(answer.toString());
                aiChatVO.setToolTrace(toolTrace);
                return aiChatVO;
            }
            throw new BaseException("AI 工具调用轮数超限，请换个问法或缩小查询范围");
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI对话处理异常", e);
            throw new BaseException("AI 服务暂不可用，请稍后重试");
        }
    }

    /**
     * 调用 AI 服务 /v1/messages 接口：
     * 组装请求体（自建对象）→ HttpClientUtil 发 POST，响应以字符串接收 →
     * 解析成 JSONObject，先校验服务端错误再返回
     */
    private JSONObject callAi(List<AiMessage> messages) {
        AiChatRequest request = new AiChatRequest();
        request.setModel(aiProperties.getModel());
        request.setMaxTokens(16000L);
        request.setSystem(systemPrompt());
        request.setMessages(messages);
        request.setTools(toolRegistry.toolDefinitions());

        String url = aiProperties.getBaseUrl().replaceAll("/+$", "") + "/v1/messages";
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("x-api-key", aiProperties.getApiKey());
        headers.put("anthropic-version", ANTHROPIC_VERSION);

        try {
            String body = JSON.toJSONString(request);
//            System.out.println(body);
            String respBody = HttpClientUtil.doPostString(url, body, headers, HTTP_TIMEOUT_MSEC);

            JSONObject resp = JSONObject.parseObject(respBody);
//            System.out.println(respBody);
            //AI 服务报错时（鉴权失败/限流等）HTTP 体为 {"type":"error","error":{"message":...}}，先识别再使用
            JSONObject error = resp.getJSONObject("error");
            if (error != null) {
                String message = error.getString("message");
                log.error("AI服务返回错误：{}", message);
                throw new BaseException("AI 服务返回错误：" + message);
            }
            return resp;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI接口调用失败", e);
            throw new BaseException("AI 服务调用失败，请稍后重试");
        }
    }

    /**
     * system 提示词：角色 + 当天日期（"今天/这个月"等相对时间由模型据此换算）+ 回答规则
     */
    private static String systemPrompt() {
        LocalDate today = LocalDate.now();
        return "你是 Fresh Market 生鲜市场管理端的经营助手，帮商家查询和分析经营数据。\n"
                + "今天是 " + today.format(DATE) + "（" + today.format(WEEK) + "），涉及\"今天/昨天/这个月/上周\"等相对时间，一律按此日期换算成具体日期后再调用工具。\n"
                + "回答规则：\n"
                + "1. 涉及商品、销量、订单、营业额、库存等数据的问题，必须先调用工具查询，只依据工具返回的数据回答；工具返回 error 或没查到就如实说明，绝不编造数字。\n"
                + "2. 工具的日期参数一律用 yyyy-MM-dd 格式。\n"
                + "3. 金额单位为元，数量按件计。\n"
                + "4. 用中文简洁回答：先给结论，再给关键明细。";
    }

    /**
     * 记录一次工具调用轨迹（入参/结果均转成 JSON 字符串）
     */
    private ToolTraceVO trace(String toolName, Map<String, Object> args, String resultJson) {
        ToolTraceVO trace = new ToolTraceVO();
        trace.setToolName(toolName);
        trace.setArguments(JSON.toJSONString(args));
        trace.setResult(resultJson);
        return trace;
    }

}
