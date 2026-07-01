package com.liu.lutu.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 行程收藏表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("travel_plan_favorite")
public class TravelPlanFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 分享id
     */
    private Long shareId;

    /**
     * 收藏类型：1-收藏，2-克隆（复制到自己行程）
     */
    private Integer favoriteType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
