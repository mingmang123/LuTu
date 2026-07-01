package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.po.*;
import com.liu.lutu.domain.vo.TravelPlanVo;
import com.liu.lutu.mapper.AiChatSessionMapper;
import com.liu.lutu.mapper.TravelPlanFavoriteMapper;
import com.liu.lutu.mapper.TravelPlanMapper;
import com.liu.lutu.mapper.TravelPlanShareMapper;
import com.liu.lutu.service.IOssService;
import com.liu.lutu.service.ITravelPlanService;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 旅行计划表 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TravelPlanServiceImpl extends ServiceImpl<TravelPlanMapper, TravelPlan> implements ITravelPlanService {

  private final TravelPlanMapper travelPlanMapper;
  private final TravelPlanItemServiceImpl travelPlanItemService;
  private final AiChatSessionMapper aiChatSessionMapper;
  private final TravelPlanShareMapper travelPlanShareMapper;
  private final TravelPlanFavoriteMapper travelPlanFavoriteMapper;
  private final IOssService ossService;

  /**
   * 获取用户的旅行计划
   * 
   * @return
   */
  @Override
  public Result<List<TravelPlanVo>> getByUserId() {
    // 获取用户ID
    Long userId = ThreadlocalUtil.getUserId();
    List<TravelPlanVo> planVos = lambdaQuery().eq(TravelPlan::getUserId, userId)
        .list()
        .stream()
        .sorted(Comparator.comparing(TravelPlan::getCreateTime))
        .map(travelPlan -> {
          TravelPlanVo travelPlanVo = new TravelPlanVo();
          // 拷贝属性
          BeanUtils.copyProperties(travelPlan, travelPlanVo);
          // 获取出行计划总金额
          BigDecimal totalCost = travelPlanItemService.getTotalCost(travelPlan.getId());
          log.info("行程ID: {}, 总金额: {}", travelPlan.getId(), totalCost);
          travelPlanVo.setTotalAmount(totalCost);
          return travelPlanVo;
        }).collect(Collectors.toList());
    // 如果没有数据，则返回空列表
    if (CollectionUtils.isEmpty(planVos)) {
      return Result.success(Collections.emptyList());
    }
    return Result.success(planVos);
  }

  /**
   * 保存用户的旅行计划
   * 
   * @param travelPlan
   * @return
   */
  @Override
  public Result saveByUserId(TravelPlan travelPlan) {
    // 获取用户ID
    Long userId = ThreadlocalUtil.getUserId();
    travelPlan.setUserId(userId);
    travelPlan.setCreateTime(LocalDateTime.now());
    save(travelPlan);
    return Result.success();
  }

  /**
   * 删除用户的旅行计划
   * 
   * @param id
   * @return
   */
  @Override
  public Result removeByUserId(Long id) {
    // 获取用户ID
    Long userId = ThreadlocalUtil.getUserId();

    // 1. 先查询该旅行计划关联的分享记录（用于删除收藏和图片）
    List<TravelPlanShare> shares = travelPlanShareMapper.selectList(
        new LambdaQueryWrapper<TravelPlanShare>()
            .eq(TravelPlanShare::getTravelPlanId, id)
            .eq(TravelPlanShare::getUserId, userId));
    List<Long> shareIds = shares.stream().map(TravelPlanShare::getId).toList();

    // 2. 删除分享封面图片
    for (TravelPlanShare share : shares) {
      if (StringUtils.hasText(share.getCoverImage())) {
        try {
          ossService.deleteFile(share.getCoverImage());
        } catch (Exception e) {
          log.warn("删除分享封面图片失败: {}, 图片URL: {}", e.getMessage(), share.getCoverImage());
        }
      }
    }

    // 3. 删除旅行计划
    travelPlanMapper.delete(new LambdaQueryWrapper<TravelPlan>()
        .eq(TravelPlan::getUserId, userId)
        .eq(TravelPlan::getId, id));

    // 4. 删除出行计划明细
    travelPlanItemService.remove(new LambdaQueryWrapper<TravelPlanItem>()
        .eq(TravelPlanItem::getTravelPlanId, id));

    // 5. 删除关联的AI对话会话
    aiChatSessionMapper.delete(new LambdaQueryWrapper<AiChatSession>()
        .eq(AiChatSession::getTravelPlanId, id)
        .eq(AiChatSession::getUserId, userId));

    // 6. 删除关联的分享记录
    travelPlanShareMapper.delete(new LambdaQueryWrapper<TravelPlanShare>()
        .eq(TravelPlanShare::getTravelPlanId, id)
        .eq(TravelPlanShare::getUserId, userId));

    // 7. 删除分享关联的收藏记录
    if (!shareIds.isEmpty()) {
      travelPlanFavoriteMapper.delete(new LambdaQueryWrapper<TravelPlanFavorite>()
          .in(TravelPlanFavorite::getShareId, shareIds));
    }

    return Result.success();
  }
}
