package com.liu.lutu.controller;


import com.liu.lutu.domain.dto.AiChatMessageDTO;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.vo.AiChatMessageVo;
import com.liu.lutu.service.IAiChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * AI对话消息表 前端控制器
 * </p>
 *
 * @author liu
 * @since 2026-04-04
 */
@RestController
@RequestMapping("/aichatmessage")
@Slf4j
@RequiredArgsConstructor
public class AiChatMessageController {

    private final IAiChatMessageService aiChatMessageService;

    /**
     * 添加对话消息
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody AiChatMessageDTO dto) {
        return aiChatMessageService.add(dto);
    }

    /**
     * 获取对话消息
     * @param sessionId 会话ID
     * @return
     */
    @GetMapping("/get")
    public Result<List<AiChatMessageVo>> get(Long sessionId) {
        return aiChatMessageService.get(sessionId);
    }






}
