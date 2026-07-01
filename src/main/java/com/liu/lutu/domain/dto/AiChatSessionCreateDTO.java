package com.liu.lutu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatSessionCreateDTO  implements Serializable {
    /**
     * 关联的旅行计划ID（AI创建/修改的那个）
     */
    private Long travelPlanId;
}
