package com.liu.lutu.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 热门目的地表
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("destination")
public class Destination implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目的地ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目的地名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String nameEn;

    /**
     * 目的地描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String imageUrl;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 最佳旅行季节
     */
    private String bestSeason;

    /**
     * 平均花费
     */
    private BigDecimal avgCost;

    /**
     * 标签，逗号分隔
     */
    private String tags;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
