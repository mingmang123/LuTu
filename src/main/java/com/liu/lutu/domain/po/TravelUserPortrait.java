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
 * 
 * </p>
 *
 * @author liu
 * @since 2026-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("travel_user_portrait")
public class TravelUserPortrait implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 同行人群
     */
    private Integer crowdType;

    /**
     * 预算档位 1=性价 2=平价 3=高端
     */
    private Integer budgetStyle;

    /**
     * '旅行风格 1=山水 2=海边 3=古镇 4=城市 5=小众'
     */
    private Integer travelStyle;

    /**
     * '旅行节奏 1=慢凑休闲 2=短途打卡 3=正常游玩',
     */
    private Integer travelRhythm;

    /**
     * 创建时间
     */
    private LocalDateTime creatTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
