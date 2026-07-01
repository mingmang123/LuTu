package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.po.*;
import com.liu.lutu.domain.vo.TravelPlanFavoriteVO;
import com.liu.lutu.mapper.TravelPlanFavoriteMapper;
import com.liu.lutu.mapper.TravelPlanItemMapper;
import com.liu.lutu.mapper.TravelPlanMapper;
import com.liu.lutu.mapper.TravelPlanShareMapper;
import com.liu.lutu.service.ITravelPlanFavoriteService;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPlanFavoriteServiceImpl extends ServiceImpl<TravelPlanFavoriteMapper, TravelPlanFavorite>
    implements ITravelPlanFavoriteService {

  private final TravelPlanFavoriteMapper favoriteMapper;
  private final TravelPlanShareMapper shareMapper;
  private final TravelPlanMapper planMapper;
  private final TravelPlanItemMapper itemMapper;

  @Override
  public Result toggleFavorite(Long shareId) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    // 检查是否已收藏
    TravelPlanFavorite existing = lambdaQuery()
        .eq(TravelPlanFavorite::getUserId, userId)
        .eq(TravelPlanFavorite::getShareId, shareId)
        .eq(TravelPlanFavorite::getFavoriteType, 1)
        .one();

    if (existing != null) {
      // 取消收藏
      removeById(existing.getId());
      return Result.success(false);
    } else {
      // 添加收藏
      TravelPlanFavorite favorite = new TravelPlanFavorite();
      favorite.setUserId(userId);
      favorite.setShareId(shareId);
      favorite.setFavoriteType(1);
      favorite.setCreateTime(LocalDateTime.now());
      save(favorite);
      return Result.success(true);
    }
  }

  @Override
  public boolean hasFavorited(Long shareId) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return false;
    }
    return lambdaQuery()
        .eq(TravelPlanFavorite::getUserId, userId)
        .eq(TravelPlanFavorite::getShareId, shareId)
        .eq(TravelPlanFavorite::getFavoriteType, 1)
        .count() > 0;
  }

  @Override
  public Result getMyFavorites(Integer page, Integer size) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    Page<TravelPlanFavorite> pageParam = new Page<>(page, size);
    Page<TravelPlanFavorite> favoritePage = lambdaQuery()
        .eq(TravelPlanFavorite::getUserId, userId)
        .eq(TravelPlanFavorite::getFavoriteType, 1)
        .orderByDesc(TravelPlanFavorite::getCreateTime)
        .page(pageParam);

    // 获取分享详情
    List<Long> shareIds = favoritePage.getRecords().stream()
        .map(TravelPlanFavorite::getShareId)
        .collect(Collectors.toList());

    if (shareIds.isEmpty()) {
      return Result.success(Page.of(page, size));
    }

    List<TravelPlanShare> shares = shareMapper.selectBatchIds(shareIds);
    Map<Long, TravelPlanShare> shareMap = shares.stream()
        .collect(Collectors.toMap(TravelPlanShare::getId, s -> s));

    // 转换为VO
    List<TravelPlanFavoriteVO> voList = favoritePage.getRecords().stream().map(favorite -> {
      TravelPlanFavoriteVO vo = new TravelPlanFavoriteVO();
      vo.setId(favorite.getId());
      vo.setUserId(favorite.getUserId());
      vo.setShareId(favorite.getShareId());
      vo.setFavoriteType(favorite.getFavoriteType());
      vo.setCreateTime(favorite.getCreateTime());

      TravelPlanShare share = shareMap.get(favorite.getShareId());
      if (share != null) {
        vo.setShareCode(share.getShareCode());
        vo.setTitle(share.getTitle());
        vo.setDescription(share.getDescription());
        vo.setCoverImage(share.getCoverImage());
        vo.setTags(share.getTags());
        vo.setViewCount(share.getViewCount());
        vo.setLikeCount(share.getLikeCount());
      }
      return vo;
    }).collect(Collectors.toList());

    Page<TravelPlanFavoriteVO> voPage = new Page<>(page, size);
    voPage.setRecords(voList);
    voPage.setTotal(favoritePage.getTotal());
    voPage.setPages(favoritePage.getPages());

    return Result.success(voPage);
  }

  @Override
  @Transactional
  public Result<Long> clonePlan(Long shareId) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    // 获取分享信息
    TravelPlanShare share = shareMapper.selectById(shareId);
    if (share == null) {
      return Result.fail("分享不存在");
    }

    // 获取原行程
    TravelPlan originalPlan = planMapper.selectById(share.getTravelPlanId());
    if (originalPlan == null) {
      return Result.fail("行程不存在");
    }

    // 获取原行程环节
    List<TravelPlanItem> originalItems = itemMapper.selectList(
        new LambdaQueryWrapper<TravelPlanItem>()
            .eq(TravelPlanItem::getTravelPlanId, originalPlan.getId())
            .orderByAsc(TravelPlanItem::getStartTime));

    // 创建新行程
    TravelPlan newPlan = new TravelPlan();
    newPlan.setUserId(userId);
    newPlan.setTitle(originalPlan.getTitle() + " (复制)");
    newPlan.setStartDate(originalPlan.getStartDate());
    newPlan.setEndDate(originalPlan.getEndDate());
    newPlan.setCreateTime(LocalDateTime.now());
    planMapper.insert(newPlan);

    // 复制行程环节
    if (originalItems != null && !originalItems.isEmpty()) {
      for (TravelPlanItem originalItem : originalItems) {
        TravelPlanItem newItem = new TravelPlanItem();
        newItem.setTravelPlanId(newPlan.getId());
        newItem.setUserId(userId);
        newItem.setItemType(originalItem.getItemType());
        newItem.setFromLocation(originalItem.getFromLocation());
        newItem.setToLocation(originalItem.getToLocation());
        newItem.setStartTime(originalItem.getStartTime());
        newItem.setEndTime(originalItem.getEndTime());
        newItem.setAmount(originalItem.getAmount());
        newItem.setCreateTime(LocalDateTime.now());
        itemMapper.insert(newItem);
      }
    }

    // 记录克隆记录
    TravelPlanFavorite favorite = new TravelPlanFavorite();
    favorite.setUserId(userId);
    favorite.setShareId(shareId);
    favorite.setFavoriteType(2);
    favorite.setCreateTime(LocalDateTime.now());
    save(favorite);

    return Result.success(newPlan.getId());
  }
}
