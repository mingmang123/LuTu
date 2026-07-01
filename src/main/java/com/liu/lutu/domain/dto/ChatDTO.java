package com.liu.lutu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {

    /**
     * 聊天内容
     */
    private String content;
    /**
     * 会话id
     */
    private Long sessionId;
    /**
     * 旅行id
     */
    private Long tripId;
}
