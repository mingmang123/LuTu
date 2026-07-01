package com.liu.lutu.domain.itf;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key
     * @return
     */
    String key() default "";

    /**
     * 每秒生成的令牌数（QPS）
     * @return
     */
    double permitsPerSecond() default 10;

    /**
     * 获取令牌超时时间（毫秒）
     * @return
     */
    long timeOut() default 1000;
}
