package com.liu.lutu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatMessageDTO implements Serializable {
    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 角色 user=用户 assistant=AI
     */
    private String role;

    /**
     * 聊天内容
     */
    private String content;
}
