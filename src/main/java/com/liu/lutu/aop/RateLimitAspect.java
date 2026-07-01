package com.liu.lutu.aop;

import com.google.common.util.concurrent.RateLimiter;
import com.liu.lutu.domain.itf.RateLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
public class RateLimitAspect {

    /**
     * 本地缓存限流器Map
     */
    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Around("@annotation(com.liu.lutu.domain.itf.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法
        Method method = signature.getMethod();

        // 获取方法上的注解
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        // 获取限流key
        String key = rateLimit.key().isEmpty() ? method.getName() : rateLimit.key();
        // 获取限流器
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(key, k -> RateLimiter.create(rateLimit.permitsPerSecond()));

        // 尝试获取令牌
        boolean acquire = rateLimiter.tryAcquire(rateLimit.timeOut(), TimeUnit.MILLISECONDS);
            if (!acquire) {
                throw new RuntimeException("访问频繁，请稍后再试");
            }

        return joinPoint.proceed();
    }

}
