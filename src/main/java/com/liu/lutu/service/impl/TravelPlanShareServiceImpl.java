package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.emun.TravelTypeEnum;
import com.liu.lutu.domain.po.*;
import com.liu.lutu.domain.vo.ShareDetailVO;
import com.liu.lutu.domain.vo.TravelPlanItemVo;
import com.liu.lutu.mapper.TravelPlanItemMapper;
import com.liu.lutu.mapper.TravelPlanMapper;
import com.liu.lutu.mapper.TravelPlanShareMapper;
import com.liu.lutu.mapper.UserMapper;
import com.liu.lutu.service.ITravelPlanShareService;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPlanShareServiceImpl extends ServiceImpl<TravelPlanShareMapper, TravelPlanShare>
    implements ITravelPlanShareService {

  private final TravelPlanShareMapper travelPlanShareMapper;
  private final TravelPlanItemMapper travelPlanItemMapper;
  private final TravelPlanMapper travelPlanMapper;
  private final UserMapper userMapper;
  private final StringRedisTemplate stringRedisTemplate;

  private static final String SHARE_VIEW_COUNT_KEY = "share:view:";
  private static final String SHARE_LIKE_COUNT_KEY = "share:like:";
  private static final String USER_LIKE_KEY = "user:like:";

  @Override
  public Result generatedByPlanId(Long travelPlanId) {
    Long userId = ThreadlocalUtil.getUserId();
    TravelPlanShare share = lambdaQuery()
        .eq(TravelPlanShare::getTravelPlanId, travelPlanId)
        .eq(TravelPlanShare::getUserId, userId)
        .one();

    if (share != null) {
      return Result.success(share.getShareCode());
    }

    // 获取行程信息作为默认标题
    TravelPlan plan = travelPlanMapper.selectById(travelPlanId);
    String title = plan != null ? plan.getTitle() : "我的行程分享";

    TravelPlanShare travelPlanShare = new TravelPlanShare();
    travelPlanShare.setTravelPlanId(travelPlanId);
    travelPlanShare.setUserId(userId);
    travelPlanShare.setTitle(title);
    travelPlanShare.setShareCode(generateUniqueShareCode());
    travelPlanShare.setStatus(1);
    travelPlanShare.setCreateTime(LocalDateTime.now());
    travelPlanShare.setUpdateTime(LocalDateTime.now());
    save(travelPlanShare);

    return Result.success(travelPlanShare.getShareCode());
  }

  @Override
  public Result queryByShareCode(String shareCode) {
    TravelPlanShare travelPlanShare = lambdaQuery()
        .eq(TravelPlanShare::getShareCode, shareCode)
        .one();

    if (travelPlanShare == null) {
      return Result.fail("分享不存在或已关闭");
    }

    List<TravelPlanItem> list = travelPlanItemMapper.selectList(
        new LambdaQueryWrapper<TravelPlanItem>()
            .eq(TravelPlanItem::getTravelPlanId, travelPlanShare.getTravelPlanId())
            .eq(TravelPlanItem::getUserId, travelPlanShare.getUserId())
            .orderByAsc(TravelPlanItem::getStartTime));

    if (CollectionUtils.isEmpty(list)) {
      return Result.success(Collections.emptyList());
    }

    List<TravelPlanItemVo> voList = new ArrayList<>();
    for (TravelPlanItem planItem : list) {
      TravelPlanItemVo vo = new TravelPlanItemVo();
      BeanUtils.copyProperties(planItem, vo);

      if (planItem.getItemType().equals(TravelTypeEnum.PLAY.getCode()) ||
          planItem.getItemType().equals(TravelTypeEnum.HOUSE.getCode())) {
        vo.setTransportType(null);
      }
      voList.add(vo);
    }
    return Result.success(voList);
  }

  @Override
  public Result updateStatus(Long travelPlanId, Integer status) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    // 通过行程ID查找分享记录
    TravelPlanShare travelPlanShare = lambdaQuery()
        .eq(TravelPlanShare::getTravelPlanId, travelPlanId)
        .eq(TravelPlanShare::getUserId, userId)
        .one();

    if (travelPlanShare == null) {
      return Result.fail("分享不存在");
    }

    travelPlanShare.setStatus(status);
    travelPlanShare.setUpdateTime(LocalDateTime.now());
    updateById(travelPlanShare);
    return Result.success();
  }

  @Override
  public Result queryStatus(Long travelPlanId) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    TravelPlanShare travelPlanShare = lambdaQuery()
        .eq(TravelPlanShare::getTravelPlanId, travelPlanId)
        .eq(TravelPlanShare::getUserId, userId)
        .one();

    if (travelPlanShare == null) {
      return Result.success();
    }

    // 返回完整的分享信息，包括标题等
    java.util.Map<String, Object> result = new java.util.HashMap<>();
    result.put("id", travelPlanShare.getId());
    result.put("shareCode", travelPlanShare.getShareCode());
    result.put("status", travelPlanShare.getStatus());
    result.put("title", travelPlanShare.getTitle());
    result.put("description", travelPlanShare.getDescription());
    result.put("coverImage", travelPlanShare.getCoverImage());
    result.put("tags", travelPlanShare.getTags());

    return Result.success(result);
  }

  @Override
  public Result createShare(Long travelPlanId, String title, String description, String coverImage, String tags) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    // 检查行程是否存在
    TravelPlan plan = travelPlanMapper.selectById(travelPlanId);
    if (plan == null) {
      return Result.fail("行程不存在");
    }

    // 检查是否已存在分享
    TravelPlanShare existingShare = lambdaQuery()
        .eq(TravelPlanShare::getTravelPlanId, travelPlanId)
        .eq(TravelPlanShare::getUserId, userId)
        .one();

    if (existingShare != null) {
      // 更新现有分享
      existingShare.setTitle(title);
      existingShare.setDescription(description);
      existingShare.setCoverImage(coverImage);
      existingShare.setTags(tags);
      existingShare.setStatus(1);
      existingShare.setUpdateTime(LocalDateTime.now());
      updateById(existingShare);
      return Result.success(existingShare.getShareCode());
    }

    // 创建新分享
    TravelPlanShare share = new TravelPlanShare();
    share.setTravelPlanId(travelPlanId);
    share.setUserId(userId);
    share.setShareCode(generateUniqueShareCode());
    share.setTitle(title);
    share.setDescription(description);
    share.setCoverImage(coverImage);
    share.setTags(tags);
    share.setStatus(1);
    share.setViewCount(0);
    share.setLikeCount(0);
    share.setCreateTime(LocalDateTime.now());
    share.setUpdateTime(LocalDateTime.now());
    save(share);

    return Result.success(share.getShareCode());
  }

  @Override
  public Result updateShare(Long travelPlanId, String title, String description, String coverImage, String tags) {
    return createShare(travelPlanId, title, description, coverImage, tags);
  }

  @Override
  public Result getShareDetail(String shareCode) {
    TravelPlanShare share = lambdaQuery()
        .eq(TravelPlanShare::getShareCode, shareCode)
        .eq(TravelPlanShare::getStatus, 1)
        .one();

    if (share == null) {
      return Result.fail("分享不存在或已关闭");
    }

    // 增加浏览量
    incrementViewCount(shareCode);

    // 获取行程信息
    TravelPlan plan = travelPlanMapper.selectById(share.getTravelPlanId());
    if (plan == null) {
      return Result.fail("行程不存在");
    }

    // 获取行程环节
    List<TravelPlanItem> items = travelPlanItemMapper.selectList(
        new LambdaQueryWrapper<TravelPlanItem>()
            .eq(TravelPlanItem::getTravelPlanId, share.getTravelPlanId())
            .orderByAsc(TravelPlanItem::getStartTime));

    // 获取作者信息
    User author = userMapper.selectById(share.getUserId());

    // 构建返回数据
    ShareDetailVO detail = new ShareDetailVO();
    detail.setId(share.getId());
    detail.setShareCode(share.getShareCode());
    detail.setTitle(share.getTitle());
    detail.setDescription(share.getDescription());
    detail.setCoverImage(share.getCoverImage());
    detail.setTags(parseTags(share.getTags()));
    detail.setViewCount(getViewCount(shareCode));
    detail.setLikeCount(getLikeCount(shareCode));
    detail.setHasLiked(hasUserLiked(shareCode));
    detail.setCreateTime(share.getCreateTime());

    // 作者信息
    ShareDetailVO.AuthorInfo authorInfo = new ShareDetailVO.AuthorInfo();
    authorInfo.setId(author != null ? author.getId() : null);
    authorInfo.setUsername(author != null ? author.getUsername() : "匿名用户");
    authorInfo.setAvatar(author != null ? author.getAvatar() : null);
    detail.setAuthor(authorInfo);

    // 行程信息
    ShareDetailVO.TravelPlanInfo planInfo = new ShareDetailVO.TravelPlanInfo();
    planInfo.setId(plan.getId());
    planInfo.setTitle(plan.getTitle());
    planInfo.setStartDate(plan.getStartDate() != null ? plan.getStartDate().toString() : null);
    planInfo.setEndDate(plan.getEndDate() != null ? plan.getEndDate().toString() : null);
    planInfo.setTotalDays((int) ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1);
    detail.setTravelPlan(planInfo);

    // 行程环节
    List<ShareDetailVO.TravelPlanItemInfo> itemList = new ArrayList<>();
    for (TravelPlanItem item : items) {
      ShareDetailVO.TravelPlanItemInfo voItem = new ShareDetailVO.TravelPlanItemInfo();
      voItem.setId(item.getId());
      voItem.setItemType(item.getItemType() != null ? item.getItemType().getCode() : null);
      voItem.setStartTime(item.getStartTime() != null ? item.getStartTime().toString() : null);
      voItem.setEndTime(item.getEndTime() != null ? item.getEndTime().toString() : null);
      voItem.setFromLocation(item.getFromLocation());
      voItem.setToLocation(item.getToLocation());
      voItem.setAmount(item.getAmount() != null ? item.getAmount().doubleValue() : null);
      voItem.setTransportType(item.getTransportType());
      itemList.add(voItem);
    }
    detail.setItems(itemList);

    return Result.success(detail);
  }

  @Override
  public void incrementViewCount(String shareCode) {
    String key = SHARE_VIEW_COUNT_KEY + shareCode;
    stringRedisTemplate.opsForValue().increment(key);
    // 设置过期时间为7天
    stringRedisTemplate.expire(key, 7, TimeUnit.DAYS);
  }

  @Override
  public Result likeShare(String shareCode) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    String userLikeKey = USER_LIKE_KEY + userId + ":" + shareCode;
    String shareLikeKey = SHARE_LIKE_COUNT_KEY + shareCode;

    // 检查是否已经点赞
    Boolean hasLiked = stringRedisTemplate.hasKey(userLikeKey);
    boolean isLiked;
    if (Boolean.TRUE.equals(hasLiked)) {
      // 取消点赞
      stringRedisTemplate.delete(userLikeKey);
      stringRedisTemplate.opsForValue().decrement(shareLikeKey);
      isLiked = false;
    } else {
      // 点赞
      stringRedisTemplate.opsForValue().set(userLikeKey, "1", 30, TimeUnit.DAYS);
      stringRedisTemplate.opsForValue().increment(shareLikeKey);
      stringRedisTemplate.expire(shareLikeKey, 30, TimeUnit.DAYS);
      isLiked = true;
    }

    // 获取最新的点赞数
    int likeCount = getLikeCount(shareCode);

    // 返回包含点赞状态和点赞数的对象
    java.util.Map<String, Object> result = new java.util.HashMap<>();
    result.put("hasLiked", isLiked);
    result.put("likeCount", likeCount);
    return Result.success(result);
  }

  public boolean hasUserLiked(String shareCode) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return false;
    }
    String userLikeKey = USER_LIKE_KEY + userId + ":" + shareCode;
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(userLikeKey));
  }

  public int getViewCount(String shareCode) {
    String key = SHARE_VIEW_COUNT_KEY + shareCode;
    String count = stringRedisTemplate.opsForValue().get(key);
    return count != null ? Integer.parseInt(count) : 0;
  }

  public int getLikeCount(String shareCode) {
    String key = SHARE_LIKE_COUNT_KEY + shareCode;
    String count = stringRedisTemplate.opsForValue().get(key);
    return count != null ? Integer.parseInt(count) : 0;
  }

  /**
   * 生成唯一的分享码
   */
  private String generateUniqueShareCode() {
    String code;
    do {
      code = RandomStringUtils.randomAlphanumeric(8);
    } while (lambdaQuery().eq(TravelPlanShare::getShareCode, code).count() > 0);
    return code;
  }

  /**
   * 解析标签字符串为列表
   */
  private List<String> parseTags(String tags) {
    if (!StringUtils.hasText(tags)) {
      return Collections.emptyList();
    }
    return Arrays.stream(tags.split(","))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }

  @Override
  public Result getHotShares(Integer page, Integer size) {
    return getShareList(page, size);
  }

  @Override
  public Result getLatestShares(Integer page, Integer size) {
    return getShareList(page, size);
  }

  @Override
  public Result searchByTag(String tag, Integer page, Integer size) {
    return getShareList(page, size);
  }

  @Override
  public Result getUserShares(Long userId) {
    LambdaQueryWrapper<TravelPlanShare> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TravelPlanShare::getUserId, userId)
        .eq(TravelPlanShare::getStatus, 1)
        .orderByDesc(TravelPlanShare::getCreateTime);
    List<TravelPlanShare> shares = list(wrapper);
    return Result.success(shares);
  }

  private Result getShareList(Integer page, Integer size) {
    Page<TravelPlanShare> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<TravelPlanShare> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TravelPlanShare::getStatus, 1)
        .orderByDesc(TravelPlanShare::getCreateTime);

    Page<TravelPlanShare> result = page(pageParam, wrapper);

    List<ShareDetailVO> list = result.getRecords().stream().map(share -> {
      ShareDetailVO detail = new ShareDetailVO();
      detail.setId(share.getId());
      detail.setShareCode(share.getShareCode());
      detail.setTitle(share.getTitle());
      detail.setDescription(share.getDescription());
      detail.setCoverImage(share.getCoverImage());
      detail.setTags(parseTags(share.getTags()));
      detail.setViewCount(getViewCount(share.getShareCode()));
      detail.setLikeCount(getLikeCount(share.getShareCode()));
      detail.setCreateTime(share.getCreateTime());

      // 作者信息
      User author = userMapper.selectById(share.getUserId());
      ShareDetailVO.AuthorInfo authorInfo = new ShareDetailVO.AuthorInfo();
      authorInfo.setId(author != null ? author.getId() : null);
      authorInfo.setUsername(author != null ? author.getUsername() : "匿名用户");
      authorInfo.setAvatar(author != null ? author.getAvatar() : null);
      detail.setAuthor(authorInfo);

      return detail;
    }).collect(Collectors.toList());

    Page<ShareDetailVO> voPage = new Page<>();
    voPage.setCurrent(result.getCurrent());
    voPage.setSize(result.getSize());
    voPage.setTotal(result.getTotal());
    voPage.setPages(result.getPages());
    voPage.setRecords(list);

    return Result.success(voPage);
  }
}
