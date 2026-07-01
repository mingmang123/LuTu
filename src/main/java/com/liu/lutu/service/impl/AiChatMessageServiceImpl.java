package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.dto.AiChatMessageDTO;
import com.liu.lutu.domain.po.AiChatMessage;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.vo.AiChatMessageVo;
import com.liu.lutu.mapper.AiChatMessageMapper;
import com.liu.lutu.service.IAiChatMessageService;
import com.liu.lutu.util.ThreadlocalUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * AI对话消息表 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-04
 */
@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage> implements IAiChatMessageService {

    /**
     * 添加对话消息
     * @param dto
     * @return
     */
    @Override
    public Result add(AiChatMessageDTO dto) {
        // 获取用户ID
        Long userId = ThreadlocalUtil.getUserId();
        if (userId == null) {
            return Result.fail("用户不存在");
        }
        return add(dto, userId);
    }

    /**
     * 添加对话消息（带用户ID）
     * @param dto
     * @param userId
     * @return
     */
    @Override
    public Result add(AiChatMessageDTO dto, Long userId) {
        if (userId == null) {
            return Result.fail("用户不存在");
        }
        AiChatMessage aiChatMessage = new AiChatMessage();
        BeanUtils.copyProperties(dto, aiChatMessage);
        aiChatMessage.setCreateTime(LocalDateTime.now());
        aiChatMessage.setUserId(userId);

        this.save(aiChatMessage);
        return Result.success();
    }

    /**
     * 获取对话消息
     * @param sessionId 会话ID
     * @return
     */
    @Override
    public Result<List<AiChatMessageVo>> get(Long sessionId) {
        Long userId = ThreadlocalUtil.getUserId();
        // 查询用户的所有对话消息
        List<AiChatMessage> list = lambdaQuery().eq(AiChatMessage::getSessionId, sessionId)
                .eq(AiChatMessage::getUserId, userId)
                .orderByAsc(AiChatMessage::getCreateTime)
                .list();

        List<AiChatMessageVo> aiChatMessageVos = new ArrayList<>();
        list.forEach(aiChatMessage -> {
            AiChatMessageVo aiChatMessageVo = new AiChatMessageVo();
            BeanUtils.copyProperties(aiChatMessage, aiChatMessageVo);
            aiChatMessageVos.add(aiChatMessageVo);
        });

        return Result.success(aiChatMessageVos);
    }

    /**
     * 删除对话消息
     * @param sessionId 会话ID
     */
    public void delete(Long sessionId) {
        Long userId = ThreadlocalUtil.getUserId();
        lambdaUpdate().eq(AiChatMessage::getSessionId, sessionId)
                .eq(AiChatMessage::getUserId, userId)
                .remove();
    }



    /**
     * 获取对话消息（限制条数）
     * @param sessionId 会话ID
     * @param limit 限制条数
     * @return
     */
    public List<AiChatMessageVo> getLimit(Long sessionId, Integer limit) {
        Long userId = ThreadlocalUtil.getUserId();
        // 查询用户的所有对话消息
        List<AiChatMessage> list = lambdaQuery().eq(AiChatMessage::getSessionId, sessionId)
                .eq(AiChatMessage::getUserId, userId)
                .orderByDesc(AiChatMessage::getCreateTime)
                .last("limit " + limit)
                .list();

        List<AiChatMessageVo> aiChatMessageVos = new ArrayList<>();
        list.forEach(aiChatMessage -> {
            AiChatMessageVo aiChatMessageVo = new AiChatMessageVo();
            BeanUtils.copyProperties(aiChatMessage, aiChatMessageVo);
            aiChatMessageVos.add(aiChatMessageVo);
        });

        return aiChatMessageVos;
    }
}
