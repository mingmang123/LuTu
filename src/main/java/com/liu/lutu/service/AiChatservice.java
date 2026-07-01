package com.liu.lutu.service;

import com.liu.lutu.domain.vo.ChatEventVO;
import com.liu.lutu.domain.vo.MessageVO;
import com.liu.lutu.util.ThreadlocalUtil;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatservice {
    /**
     * 处理流式聊天请求，返回服务器发送事件（SSE）格式的响应流
     * @param question 用户输入的聊天问题
     * @return 包含逐条聊天响应的响应式数据流，通过Server-Sent Events协议传输
     */
    Flux<ChatEventVO> chatStream(String question, Long sessionId);
    
    /**
     * 处理普通聊天请求，返回完整的聊天响应
     * @param question 用户输入的聊天问题
     * @param sessionId 会话ID
     * @return 完整的聊天响应
     */
    String chat(String question, Long sessionId, Long tripId);

    /**
     * 停止流式聊天会话
     * @param sessionId 会话ID
     */
    void stop(String sessionId);

    /**
     * 获取对话id，规则：用户id_会话id
     *
     * @param sessionId 会话id
     * @return 对话id
     */
    static String getConversationId(Long sessionId) {
        return ThreadlocalUtil.getUserId().toString() + "_" + sessionId.toString();
    }

    /**
     * 查询单个历史对话详情
     * @param  sessionId 会话ID
     * @return 对话记录列表
     */
    List<MessageVO> queryBySessionId(Long sessionId);
}
