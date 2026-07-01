package com.liu.lutu.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 旅行攻略表
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("travel_guide")
public class TravelGuide implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 攻略ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 攻略标题
     */
    private String title;

    /**
     * 攻略摘要
     */
    private String summary;

    /**
     * 攻略内容
     */
    private String content;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 关联目的地ID
     */
    private Long destinationId;

    /**
     * 作者
     */
    private String author;

    /**
     * 阅读时长（分钟）
     */
    private Integer readTime;

    /**
     * 标签
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
