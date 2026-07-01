package com.liu.lutu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.dto.AiChatMessageDTO;
import com.liu.lutu.domain.po.AiChatMessage;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.vo.AiChatMessageVo;

import java.util.List;

/**
 * <p>
 * AI对话消息表 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-04
 */
public interface IAiChatMessageService extends IService<AiChatMessage> {

    /**
     * 添加对话消息
     * @param dto
     * @return
     */
    Result add(AiChatMessageDTO dto);

    /**
     * 添加对话消息（带用户ID）
     * @param dto
     * @param userId
     * @return
     */
    Result add(AiChatMessageDTO dto, Long userId);

    /**
     * 获取对话消息
     * @param sessionId 会话ID
     * @return
     */
    Result<List<AiChatMessageVo>> get(Long sessionId);
}
