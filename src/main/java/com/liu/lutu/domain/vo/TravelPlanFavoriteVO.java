package com.liu.lutu.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 行程收藏 VO
 */
@Data
public class TravelPlanFavoriteVO {

    private Long id;

    private Long userId;

    private Long shareId;

    private Integer favoriteType;

    private LocalDateTime createTime;

    // 分享详情
    private String shareCode;
    private String title;
    private String description;
    private String coverImage;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
}
