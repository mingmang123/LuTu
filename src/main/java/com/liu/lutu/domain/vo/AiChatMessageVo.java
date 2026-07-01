package com.liu.lutu.domain.vo;

import java.io.Serializable;


public class AiChatMessageVo implements Serializable {
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

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
