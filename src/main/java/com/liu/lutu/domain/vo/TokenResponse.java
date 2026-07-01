package com.liu.lutu.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 双Token响应VO
 */
@Data
@Builder
public class TokenResponse {
    
    /**
     * 访问令牌（短有效期）
     */
    private String accessToken;
    
    /**
     * 刷新令牌（长有效期）
     */
    private String refreshToken;
    
    /**
     * Access Token过期时间戳（秒）
     */
    private Long accessExpire;
    
    /**
     * Refresh Token过期时间戳（秒）
     */
    private Long refreshExpire;
    
    /**
     * 是否为记住我模式（Refresh Token有效期更长）
     */
    private Boolean rememberMe;
}
