package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.dto.AiChatSessionCreateDTO;
import com.liu.lutu.domain.po.AiChatSession;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlan;
import com.liu.lutu.domain.vo.AiChatSessionVo;
import com.liu.lutu.mapper.AiChatSessionMapper;
import com.liu.lutu.service.IAiChatSessionService;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * AI对话会话表 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession> implements IAiChatSessionService {
    private final AiChatSessionMapper aiChatSessionMapper;
    private final TravelPlanServiceImpl travelPlanService;
    private final AiChatMessageServiceImpl aiChatMessageService;
    private final ChatMemory chatMemory;

    /**
     * 创建AI对话会话
     * @param dto
     * @return
     */
    @Override
    public Result<AiChatSessionVo> create(AiChatSessionCreateDTO dto) {
        AiChatSession aiChatSession = new AiChatSession();

        //获取用户ID
        Long userId = ThreadlocalUtil.getUserId();
        //拿到标题
        TravelPlan travelPlan = travelPlanService.lambdaQuery().eq(TravelPlan::getId, dto.getTravelPlanId())
                .eq(TravelPlan::getUserId, userId)
                .one();
        if (travelPlan == null) {
            return Result.fail("旅行计划不存在");
        }

        aiChatSession.setUserId(userId);
        aiChatSession.setTravelPlanId(dto.getTravelPlanId());
        aiChatSession.setTitle(travelPlan.getTitle());
        aiChatSession.setCreateTime(LocalDateTime.now());
        aiChatSession.setUpdateTime(LocalDateTime.now());
        //返回会话ID
        aiChatSessionMapper.insert(aiChatSession);


        AiChatSessionVo aiChatSessionVo = new AiChatSessionVo();
        BeanUtils.copyProperties(aiChatSession, aiChatSessionVo);
        // MyBatis-Plus会在插入后自动设置ID到实体对象中
        aiChatSessionVo.setId(aiChatSession.getId());

        return Result.success(aiChatSessionVo);
    }

    /**
     * 获取AI对话会话
     * @return
     */
    @Override
    public Result<List<AiChatSessionVo>> get() {
        Long userId = ThreadlocalUtil.getUserId();
        List<AiChatSession> aiChatSessions = aiChatSessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId));

        if (CollectionUtils.isEmpty(aiChatSessions)) {
            return Result.success(new ArrayList<>());
        }
        List<AiChatSessionVo> aiChatSessionVos = new ArrayList<>();
        aiChatSessions.forEach(aiChatSession -> {
            AiChatSessionVo aiChatSessionVo = new AiChatSessionVo();
            BeanUtils.copyProperties(aiChatSession, aiChatSessionVo);
            aiChatSessionVos.add(aiChatSessionVo);
        });
        return Result.success(aiChatSessionVos);
    }

    /**
     * 删除AI对话会话
     * @param id 会话id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delete(Long id) {
        Long userId = ThreadlocalUtil.getUserId();
        //删除会话
        aiChatSessionMapper.delete(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getId, id)
                .eq(AiChatSession::getUserId, userId));
        //删除消息
        aiChatMessageService.delete(id);
        // 直接拼接conversationId，避免ThreadLocal为空导致NPE
        String conversationId = userId + "_" + id;
        chatMemory.clear(conversationId);

        return Result.success();
    }
}
