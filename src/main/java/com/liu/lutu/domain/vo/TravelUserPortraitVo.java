package com.liu.lutu.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelUserPortraitVo implements Serializable {
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
}
