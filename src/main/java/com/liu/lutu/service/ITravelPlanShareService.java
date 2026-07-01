package com.liu.lutu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlanShare;

/**
 * 行程分享服务接口
 */
public interface ITravelPlanShareService extends IService<TravelPlanShare> {

  /**
   * 生成分享码
   *
   * @param travelPlanId 行程ID
   * @return 分享码
   */
  Result generatedByPlanId(Long travelPlanId);

  /**
   * 分享码查询
   *
   * @param shareCode 分享码
   * @return 分享详情
   */
  Result queryByShareCode(String shareCode);

  /**
   * 修改分享码状态
   *
   * @param travelPlanId 行程ID
   * @param status       状态 1开启，0关闭
   * @return 结果
   */
  Result updateStatus(Long travelPlanId, Integer status);

  /**
   * 分享码状态查询
   *
   * @param id 行程ID
   * @return 状态
   */
  Result queryStatus(Long id);

  /**
   * 创建带详细信息的分享
   *
   * @param travelPlanId 行程ID
   * @param title        分享标题
   * @param description  分享描述
   * @param coverImage   封面图片
   * @param tags         标签
   * @return 分享信息
   */
  Result createShare(Long travelPlanId, String title, String description, String coverImage, String tags);

  /**
   * 获取分享详情（包含行程环节）
   *
   * @param shareCode 分享码
   * @return 完整分享详情
   */
  Result getShareDetail(String shareCode);

  /**
   * 获取热门分享列表
   *
   * @param page 页码
   * @param size 每页数量
   * @return 分享列表
   */
  Result getHotShares(Integer page, Integer size);

  /**
   * 获取最新分享列表
   *
   * @param page 页码
   * @param size 每页数量
   * @return 分享列表
   */
  Result getLatestShares(Integer page, Integer size);

  /**
   * 根据标签搜索分享
   *
   * @param tag  标签
   * @param page 页码
   * @param size 每页数量
   * @return 分享列表
   */
  Result searchByTag(String tag, Integer page, Integer size);

  /**
   * 增加浏览次数
   *
   * @param shareCode 分享码
   */
  void incrementViewCount(String shareCode);

  /**
   * 点赞分享
   *
   * @param shareCode 分享码
   * @return 当前点赞数
   */
  Result likeShare(String shareCode);

  /**
   * 检查用户是否已点赞
   *
   * @param shareCode 分享码
   * @return 是否已点赞
   */
  boolean hasUserLiked(String shareCode);

  /**
   * 获取用户的分享列表
   *
   * @param userId 用户ID
   * @return 分享列表
   */
  Result getUserShares(Long userId);

  /**
   * 更新分享信息
   *
   * @param travelPlanId 行程ID
   * @param title        分享标题
   * @param description  分享描述
   * @param coverImage   封面图片
   * @param tags         标签
   * @return 结果
   */
  Result updateShare(Long travelPlanId, String title, String description, String coverImage, String tags);

}
