package com.liu.lutu.controller;

import cn.hutool.json.JSONUtil;
import com.liu.lutu.domain.po.Destination;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelGuide;
import com.liu.lutu.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 首页数据 前端控制器
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/home")
@Tag(name = "首页数据", description = "首页推荐、统计等数据")
@Slf4j
@RequiredArgsConstructor
public class HomeController {

  private final IDestinationService destinationService;
  private final ITravelGuideService travelGuideService;
  private final ITravelPlanService travelPlanService;
  private final IUserService userService;
  private final ITravelPlanShareService travelPlanShareService;
  private final StringRedisTemplate redisTemplate;

  private static final String STATS_CACHE_KEY = "home:stats";
  private static final long STATS_CACHE_TTL = 5;

  /**
   * 获取首页所有数据
   * 
   * @return 首页数据
   */
  @GetMapping("/data")
  @Operation(summary = "获取首页数据", description = "获取首页所有展示数据")
  public Result<Map<String, Object>> getHomeData() {
    Map<String, Object> data = new HashMap<>();

    // 热门目的地
    Result<List<Destination>> hotDestinations = destinationService.getHotDestinations(4);
    data.put("hotDestinations", hotDestinations.getData());

    // 推荐攻略
    Result<List<TravelGuide>> recommendedGuides = travelGuideService.getRecommendedGuides(4);
    data.put("recommendedGuides", recommendedGuides.getData());

    // 当前季节
    String currentSeason = getCurrentSeason();
    data.put("currentSeason", currentSeason);

    // 季节推荐
    Result<List<Destination>> seasonDestinations = destinationService.getDestinationsBySeason(currentSeason);
    data.put("seasonDestinations", seasonDestinations.getData());

    return Result.success(data);
  }

  /**
   * 获取热门目的地
   * 
   * @param limit 数量限制
   * @return 目的地列表
   */
  @GetMapping("/destinations/hot")
  @Operation(summary = "热门目的地", description = "获取热门目的地列表")
  public Result<List<Destination>> getHotDestinations(
      @RequestParam(defaultValue = "6") Integer limit) {
    return destinationService.getHotDestinations(limit);
  }

  /**
   * 获取推荐攻略
   * 
   * @param limit 数量限制
   * @return 攻略列表
   */
  @GetMapping("/guides/recommended")
  @Operation(summary = "推荐攻略", description = "获取推荐攻略列表")
  public Result<List<TravelGuide>> getRecommendedGuides(
      @RequestParam(defaultValue = "4") Integer limit) {
    return travelGuideService.getRecommendedGuides(limit);
  }

  /**
   * 获取季节推荐
   * 
   * @return 目的地列表
   */
  @GetMapping("/destinations/season")
  @Operation(summary = "季节推荐", description = "获取当前季节推荐目的地")
  public Result<List<Destination>> getSeasonDestinations() {
    String season = getCurrentSeason();
    return destinationService.getDestinationsBySeason(season);
  }

  /**
   * 获取推荐目的地（为你推荐）
   * 
   * @param limit 数量限制
   * @return 目的地列表
   */
  @GetMapping("/destinations/recommended")
  @Operation(summary = "推荐目的地", description = "获取为你推荐的目的地列表")
  public Result<List<Destination>> getRecommendedDestinations(
      @RequestParam(defaultValue = "3") Integer limit) {
    return destinationService.getHotDestinations(limit);
  }

  /**
   * 获取当前季节
   * 
   * @return 季节名称
   */
  private String getCurrentSeason() {
    int month = LocalDateTime.now().getMonthValue();
    if (month >= 3 && month <= 5) {
      return "春季";
    } else if (month >= 6 && month <= 8) {
      return "夏季";
    } else if (month >= 9 && month <= 11) {
      return "秋季";
    } else {
      return "冬季";
    }
  }

  /**
   * 获取统计数据
   * 
   * @return 统计数据
   */
  @GetMapping("/statistics")
  @Operation(summary = "统计数据", description = "获取首页统计数据")
  public Result<Map<String, Object>> getStatistics() {
    // 尝试从缓存获取
    String cached = redisTemplate.opsForValue().get(STATS_CACHE_KEY);
    if (cached != null) {
      log.debug("从缓存获取统计数据");
      @SuppressWarnings("unchecked")
      Map<String, Object> cachedStats = JSONUtil.toBean(cached, Map.class);
      return Result.success(cachedStats);
    }

    Map<String, Object> stats = new HashMap<>();

    // 旅行计划数量
    long planCount = travelPlanService.count();
    stats.put("plans", planCount);

    // 用户数量
    long userCount = userService.count();
    stats.put("users", userCount);

    // 分享数量
    long shareCount = travelPlanShareService.count();
    stats.put("shares", shareCount);

    // 缓存结果（5分钟TTL，统计数据不需要实时精确）
    redisTemplate.opsForValue().set(STATS_CACHE_KEY, JSONUtil.toJsonStr(stats), STATS_CACHE_TTL, TimeUnit.MINUTES);

    return Result.success(stats);
  }
}
