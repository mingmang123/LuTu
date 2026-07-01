package com.liu.lutu.service;

import com.liu.lutu.domain.dto.AiChatSessionCreateDTO;
import com.liu.lutu.domain.po.AiChatSession;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.vo.AiChatSessionVo;

import java.util.List;

/**
 * <p>
 * AI对话会话表 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-04
 */
public interface IAiChatSessionService extends IService<AiChatSession> {
    /**
     * 创建AI对话会话
     * @param dto
     * @return
     */
    Result<AiChatSessionVo> create(AiChatSessionCreateDTO dto);

    /**
     * 获取AI对话会话
     * @return
     */
    Result<List<AiChatSessionVo>> get();

    /**
     * 删除AI对话会话
     * @param id 会话id
     * @return
     */
    Result delete(Long id);
}
