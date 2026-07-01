package com.liu.lutu.Interceptor;

import cn.hutool.core.util.StrUtil;
import com.liu.lutu.util.JwtUtil;
import com.liu.lutu.util.ThreadlocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的Access Token
        String token = request.getHeader("Authorization");
        
        // 如果token为空，则返回401
        if(StrUtil.isBlank(token)){
            writeError(response, 401, "未登录或Token已过期");
            return false;
        }
        
        // 去掉 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 解析JWT
        Long userId = jwtUtil.parseToken(token);
        
        // JWT解析失败（过期或无效）
        if(userId == null){
            writeError(response, 401, "Access Token已过期，请使用Refresh Token刷新");
            return false;
        }
        
        // 从Redis中获取当前有效的Access Token
        String redisAccessToken = redisTemplate.opsForValue().get("user:access:" + userId);
        
        // 若Redis中不存在或不相等，说明Token已被刷新或用户已登出
        if(StrUtil.isBlank(redisAccessToken)) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }
        
        if(!redisAccessToken.equals(token)) {
            // Token不一致，可能是被挤下线或已刷新
            writeError(response, 401, "Token已失效，请重新登录");
            return false;
        }

        // 将用户ID存入线程本地变量
        ThreadlocalUtil.set(userId);
        return true;
    }
    
    /**
     * 写入错误响应
     */
    private void writeError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":null}");
    }

    // 请求结束清空 ThreadLocal（必须，防止内存泄漏）
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadlocalUtil.remove();
    }
}
