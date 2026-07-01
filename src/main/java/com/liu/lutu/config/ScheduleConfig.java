package com.liu.lutu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring定时任务功能
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {
    // 启用Spring定时任务
    // 定时任务定义在 com.liu.lutu.job 包下
}
