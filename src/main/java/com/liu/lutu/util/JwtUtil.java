package com.liu.lutu.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // 从 yml 读取密钥
    @Value("${sa-token.jwt-secret-key}")
    private String secret;


    //生成token
    public String createToken(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        // 创建时间
        payload.put("createTime", System.currentTimeMillis());
        // 60分钟过期
        payload.put("expireTime", System.currentTimeMillis() + 60 * 60 * 1000);
        // 3. 生成JWT（默认HS256算法）
        return JWTUtil.createToken(payload, secret.getBytes());


    }

    //解析token
    public Long parseToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            // 设置密钥
            jwt.setKey(secret.getBytes());
            // 验证签名和过期时间
            if (!jwt.verify() || !jwt.validate(0)) {
                return null;
            }
            // 获取用户id
            String userId = jwt.getPayload("userId").toString();
            return Long.valueOf(userId);
        } catch (Exception e) {
            // Token 格式不正确（不是合法 JWT）或解析异常
            return null;
        }
    }

}
