package com.liu.lutu.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatSessionVo implements Serializable {
    /**
     * 会话ID
     */
    private Long id;

    /**
     * 关联的旅行计划ID（AI创建/修改的那个）
     */
    private Long travelPlanId;


    /**
     * 会话标题
     */
    private String title;

}
