package com.fresh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理端 AI 对话提问时传递的数据
 */
@Data
@ApiModel(value = "AiChatDTO", description = "管理端AI对话提问时传递的数据")
public class AiChatDTO implements Serializable {

    /**
     * 商家的问题，如"今天卖掉了几个阳光玫瑰葡萄"
     */
    @ApiModelProperty(value = "问题内容", required = true)
    private String question;

}
