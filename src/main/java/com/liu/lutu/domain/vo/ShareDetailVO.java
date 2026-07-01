package com.liu.lutu.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享详情VO
 */
@Data
public class ShareDetailVO {

  /**
   * 分享ID
   */
  private Long id;

  /**
   * 分享码
   */
  private String shareCode;

  /**
   * 分享标题
   */
  private String title;

  /**
   * 分享描述
   */
  private String description;

  /**
   * 封面图片
   */
  private String coverImage;

  /**
   * 标签列表
   */
  private List<String> tags;

  /**
   * 浏览次数
   */
  private Integer viewCount;

  /**
   * 点赞次数
   */
  private Integer likeCount;

  /**
   * 当前用户是否已点赞
   */
  private Boolean hasLiked;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 作者信息
   */
  private AuthorInfo author;

  /**
   * 行程信息
   */
  private TravelPlanInfo travelPlan;

  /**
   * 行程环节列表
   */
  private List<TravelPlanItemInfo> items;

  /**
   * 作者信息内部类
   */
  @Data
  public static class AuthorInfo {
    private Long id;
    private String username;
    private String avatar;
  }

  /**
   * 行程信息内部类
   */
  @Data
  public static class TravelPlanInfo {
    private Long id;
    private String title;
    private String destination;
    private String startDate;
    private String endDate;
    private Integer totalDays;
    private Double totalCost;
  }

  /**
   * 行程环节信息内部类
   */
  @Data
  public static class TravelPlanItemInfo {
    private Long id;
    private Integer itemType;
    private String startTime;
    private String endTime;
    private String fromLocation;
    private String toLocation;
    private Double amount;
    private String transportType;
  }

}
