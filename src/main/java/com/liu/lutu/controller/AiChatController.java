package com.liu.lutu.controller;

import com.liu.lutu.domain.dto.ChatDTO;
import com.liu.lutu.domain.itf.RateLimit;
import com.liu.lutu.domain.vo.ChatEventVO;
import com.liu.lutu.domain.vo.MessageVO;
import com.liu.lutu.service.AiChatservice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/aichat")
@Slf4j
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatservice aiChatService;

    /**
     * 处理流式聊天请求，返回服务器发送事件（SSE）格式的响应流
     *
     * @param chatDTO 用户输入的聊天问题
     * @return 包含逐条聊天响应的响应式数据流，通过Server-Sent Events协议传输
     */
    @PostMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chatStream(@RequestBody ChatDTO chatDTO) {
        return aiChatService.chatStream(chatDTO.getContent(), chatDTO.getSessionId());
    }

    /**
     * 停止流式聊天会话
     *
     * @param sessionId 会话ID
     */
    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.aiChatService.stop(sessionId);
    }

    /**
     * 流式聊天查询单个历史对话详情
     *
     * @return 对话记录列表
     */
    @GetMapping("/{sessionId}")
    public List<MessageVO> queryBySessionId(@PathVariable("sessionId") Long sessionId) {
        return this.aiChatService.queryBySessionId(sessionId);
    }

    
    /**
     * 处理普通聊天请求，返回完整的聊天响应
     *
     * @param content 用户输入的聊天问题
     * @param sessionId 会话ID
     * @param tripId 旅行ID（可选）
     * @return 完整的聊天响应
     */
    @RateLimit(permitsPerSecond = 10, timeOut = 100)
    @GetMapping("chat")
    public String chat(@RequestParam String content, @RequestParam Long sessionId, @RequestParam(required = false) Long tripId) {
            return aiChatService.chat(content, sessionId, tripId);

    }
    
    /**
     * 处理普通聊天请求（POST方式），返回完整的聊天响应
     *
     * @param chatDTO 用户输入的聊天问题
     * @return 完整的聊天响应
     */
    @RateLimit(permitsPerSecond = 10, timeOut = 100)
    @PostMapping("chat")
    public String chat(@RequestBody ChatDTO chatDTO) {
        return aiChatService.chat(chatDTO.getContent(), chatDTO.getSessionId(), chatDTO.getTripId());
    }
}
