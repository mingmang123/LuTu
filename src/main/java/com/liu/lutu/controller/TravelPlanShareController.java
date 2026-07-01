package com.liu.lutu.controller;

import com.liu.lutu.domain.itf.RateLimit;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.service.ITravelPlanShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 行程分享控制器
 */
@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
public class TravelPlanShareController {

  private final ITravelPlanShareService travelPlanShareService;

  /**
   * 生成分享码
   * 
   * @param travelPlanId 行程ID
   * @return 分享链接
   */
  @PostMapping("/generate")
  public Result generateShare(@RequestParam Long travelPlanId) {
    return travelPlanShareService.generatedByPlanId(travelPlanId);
  }

  /**
   * 创建带详细信息的分享
   * 
   * @param travelPlanId 行程ID
   * @param title        分享标题
   * @param description  分享描述
   * @param coverImage   封面图片
   * @param tags         标签（逗号分隔）
   * @return 分享信息
   */
  @PostMapping("/create")
  public Result createShare(
      @RequestParam Long travelPlanId,
      @RequestParam String title,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String coverImage,
      @RequestParam(required = false) String tags) {
    return travelPlanShareService.createShare(travelPlanId, title, description, coverImage, tags);
  }

  /**
   * 获取分享详情（包含行程环节）
   * 
   * @param shareCode 分享码
   * @return 分享详情
   */
  @RateLimit(permitsPerSecond = 20, timeOut = 1000)
  @GetMapping("/detail/{shareCode}")
  public Result getShareDetail(@PathVariable String shareCode) {
    return travelPlanShareService.getShareDetail(shareCode);
  }

  /**
   * 根据分享码查询（旧接口兼容）
   * 
   * @param shareCode 分享码
   * @return 行程环节列表
   */
  @RateLimit(permitsPerSecond = 20, timeOut = 1000)
  @GetMapping("/query")
  public Result queryByShareCode(@RequestParam String shareCode) {
    return travelPlanShareService.queryByShareCode(shareCode);
  }

  /**
   * 获取热门分享列表
   * 
   * @param page 页码
   * @param size 每页数量
   * @return 分享列表
   */
  @GetMapping("/hot")
  public Result getHotShares(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size) {
    return travelPlanShareService.getHotShares(page, size);
  }

  /**
   * 获取最新分享列表
   * 
   * @param page 页码
   * @param size 每页数量
   * @return 分享列表
   */
  @GetMapping("/latest")
  public Result getLatestShares(
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size) {
    return travelPlanShareService.getLatestShares(page, size);
  }

  /**
   * 根据标签搜索分享
   * 
   * @param tag  标签
   * @param page 页码
   * @param size 每页数量
   * @return 分享列表
   */
  @GetMapping("/search")
  public Result searchByTag(
      @RequestParam String tag,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size) {
    return travelPlanShareService.searchByTag(tag, page, size);
  }

  /**
   * 点赞分享
   *
   * @param shareCode 分享码
   * @return 点赞结果
   */
  @PostMapping("/like/{shareCode}")
  public Result likeShare(@PathVariable String shareCode) {
    return travelPlanShareService.likeShare(shareCode);
  }

  /**
   * 检查用户是否已点赞
   *
   * @param shareCode 分享码
   * @return 是否已点赞
   */
  @GetMapping("/liked/{shareCode}")
  public Result checkLikeStatus(@PathVariable String shareCode) {
    return Result.success(travelPlanShareService.hasUserLiked(shareCode));
  }

  /**
   * 获取当前用户的分享列表
   * 
   * @return 分享列表
   */
  @GetMapping("/my")
  public Result getMyShares() {
    // 从ThreadLocal获取用户ID
    Long userId = com.liu.lutu.util.ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }
    return travelPlanShareService.getUserShares(userId);
  }

  /**
   * 修改分享状态
   * 
   * @param travelPlanId 行程ID
   * @param status       状态 1开启，0关闭
   * @return 结果
   */
  @PostMapping("/status/{travelPlanId}")
  public Result updateStatus(
      @PathVariable Long travelPlanId,
      @RequestParam Integer status) {
    return travelPlanShareService.updateStatus(travelPlanId, status);
  }

  /**
   * 查询分享状态
   * 
   * @param travelPlanId 行程ID
   * @return 状态
   */
  @GetMapping("/status")
  public Result queryStatus(@RequestParam Long travelPlanId) {
    return travelPlanShareService.queryStatus(travelPlanId);
  }

  /**
   * 更新分享信息
   * 
   * @param travelPlanId 行程ID
   * @param title        分享标题
   * @param description  分享描述
   * @param coverImage   封面图片
   * @param tags         标签（逗号分隔）
   * @return 结果
   */
  @PostMapping("/update")
  public Result updateShare(
      @RequestParam Long travelPlanId,
      @RequestParam String title,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String coverImage,
      @RequestParam(required = false) String tags) {
    return travelPlanShareService.updateShare(travelPlanId, title, description, coverImage, tags);
  }

}
