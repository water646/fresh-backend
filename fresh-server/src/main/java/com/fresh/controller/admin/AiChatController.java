package com.fresh.controller.admin;

import com.fresh.dto.AiChatDTO;
import com.fresh.result.Result;
import com.fresh.service.AiChatService;
import com.fresh.vo.AiChatVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 AI 对话相关接口：商家用自然语言查询经营数据（function calling）
 */
@RestController
@RequestMapping("/admin/ai")
@Api(tags = "管理端AI对话相关接口")
@Slf4j
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * AI 对话提问（需管理端 token）
     * @param aiChatDTO 问题内容，如"今天卖掉了几个阳光玫瑰葡萄"
     * @return AI 的回答 + 本轮工具调用轨迹
     */
    @PostMapping("/chat")
    @ApiOperation("AI对话提问")
    public Result<AiChatVO> chat(@RequestBody AiChatDTO aiChatDTO) {
        log.info("管理端AI提问：{}", aiChatDTO.getQuestion());
        AiChatVO aiChatVO = aiChatService.chat(aiChatDTO.getQuestion());
        return Result.success(aiChatVO);
    }
}
