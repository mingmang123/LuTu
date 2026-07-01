package com.liu.lutu.config;

import com.liu.lutu.Interceptor.UserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全配置类
 * 包含CORS、拦截器等安全配置
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

  private final UserInterceptor userInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(userInterceptor)
        // 所有路径都需要验证
        .addPathPatterns("/**")
        // 排除登录和注册的路径（包含/api前缀）
        .excludePathPatterns(
            "/user/sendlogin",
            "/user/captchalogin",
            "/user/login",
            "/api/user/sendlogin",
            "/api/user/captchalogin",
            "/api/user/login",
            // 分享相关接口（公开访问）
            "/travelplanshare/query",
            "/api/travelplanshare/query",
            "/share/detail/**",
            "/api/share/detail/**",
            "/share/hot",
            "/api/share/hot",
            "/share/latest",
            "/api/share/latest",
            "/share/search",
            "/api/share/search",
            "/file/upload",
            "/file/upload/batch",
            "/file/upload/token",
            "/file/delete",
            // 首页相关接口（公开访问）
            "/home/**",
            "/api/home/**",
            "/error");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    // 配置CORS，允许所有源（生产环境应配置具体域名）
    registry.addMapping("/**")
        // 允许所有源，或者配置具体的域名
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
