package com.liu.lutu.controller;


import com.liu.lutu.domain.dto.AiChatSessionCreateDTO;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.vo.AiChatSessionVo;
import com.liu.lutu.service.IAiChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * AI对话会话表 前端控制器
 * </p>
 *
 * @author liu
 * @since 2026-04-04
 */
@RestController
@RequestMapping("/aichatsession")
@Slf4j
@RequiredArgsConstructor
public class AiChatSessionController {

    private final IAiChatSessionService aiChatSessionService;

    /**
     * 创建AI对话会话
     * @param dto 只包含关联的旅行计划ID
     * @return
     */
    @PostMapping("/create")
    public Result<AiChatSessionVo> create(@RequestBody AiChatSessionCreateDTO dto) {
        return aiChatSessionService.create(dto);
    }

    /**
     * 获取所有对话会话
     * @param
     * @return
     */
    @PostMapping("/get")
    public Result<List<AiChatSessionVo>> get() {
        return aiChatSessionService.get();
    }

    /**
     * 删除AI对话会话同时删除对话消息
     * @param id 会话id
     * @return
     */
    @PostMapping("/delete")
    public Result delete(@RequestParam Long id) {
        return aiChatSessionService.delete(id);
    }

}
