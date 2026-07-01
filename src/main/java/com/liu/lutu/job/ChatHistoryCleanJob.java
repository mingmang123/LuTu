package com.liu.lutu.job;

import com.liu.lutu.domain.po.AiChatSession;
import com.liu.lutu.mapper.AiChatSessionMapper;
import com.liu.lutu.memory.RedisChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * AI对话历史清理定时任务
 * 解决Redis对话历史无过期时间、内存持续增长的问题
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCleanJob {
    
    private final StringRedisTemplate redisTemplate;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final RedisChatMemory chatMemory;
    
    // Redis key前缀
    private static final String CHAT_PREFIX = "CHAT:";
    
    // 7天未活跃则清理
    private static final int INACTIVE_DAYS = 7;
    
    /**
     * 每天凌晨2点执行清理任务
     * 清理：已删除会话的聊天记录 + 7天未活跃的会话
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanChatHistory() {
        log.info("开始执行AI对话历史清理任务...");
        
        int deletedCount = 0;
        int activeRenewCount = 0;
        
        try {
            // 1. 获取所有CHAT:开头的key
            Set<String> chatKeys = redisTemplate.keys(CHAT_PREFIX + "*");
            if (chatKeys == null || chatKeys.isEmpty()) {
                log.info("没有需要清理的对话历史");
                return;
            }
            
            log.info("发现 {} 个对话历史记录", chatKeys.size());
            
            for (String key : chatKeys) {
                try {
                    // 提取conversationId (格式: userId_sessionId)
                    String conversationId = key.substring(CHAT_PREFIX.length());
                    
                    // 解析userId和sessionId
                    String[] parts = conversationId.split("_", 2);
                    if (parts.length != 2) {
                        log.warn("无效的conversationId格式: {}", conversationId);
                        continue;
                    }
                    
                    Long userId = Long.valueOf(parts[0]);
                    Long sessionId = Long.valueOf(parts[1]);
                    
                    // 2. 检查会话是否存在
                    AiChatSession session = aiChatSessionMapper.selectById(sessionId);
                    
                    boolean shouldDelete = false;
                    
                    if (session == null) {
                        // 会话已被删除，清理聊天记录
                        shouldDelete = true;
                        log.debug("会话 {} 已删除，清理聊天记录", sessionId);
                    } else {
                        // 3. 检查最后活跃时间
                        LocalDateTime lastActive = session.getUpdateTime() != null 
                            ? session.getUpdateTime() 
                            : session.getCreateTime();
                        
                        long daysInactive = ChronoUnit.DAYS.between(lastActive, LocalDateTime.now());
                        
                        if (daysInactive > INACTIVE_DAYS) {
                            // 7天未活跃，清理聊天记录
                            shouldDelete = true;
                            log.debug("会话 {} 已{}天未活跃，清理聊天记录", sessionId, daysInactive);
                        } else {
                            // 活跃会话，续期TTL
                            activeRenewCount++;
                            log.debug("会话 {} 仍活跃（{}天前），续期TTL", sessionId, daysInactive);
                        }
                    }
                    
                    // 4. 执行删除
                    if (shouldDelete) {
                        redisTemplate.delete(key);
                        deletedCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("处理key {} 时发生错误", key, e);
                }
            }
            
            log.info("AI对话历史清理完成，共清理 {} 条记录，续期 {} 条活跃记录", deletedCount, activeRenewCount);
            
        } catch (Exception e) {
            log.error("AI对话历史清理任务执行失败", e);
        }
    }
    
    /**
     * 每周日凌晨3点执行深度清理
     * 清理：所有即将过期（TTL < 7天）或永久有效的聊天记录
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void deepCleanChatHistory() {
        log.info("开始执行AI对话历史深度清理任务...");
        
        int deletedCount = 0;
        int renewedCount = 0;
        
        try {
            Set<String> chatKeys = redisTemplate.keys(CHAT_PREFIX + "*");
            if (chatKeys == null || chatKeys.isEmpty()) {
                log.info("没有需要深度清理的对话历史");
                return;
            }
            
            for (String key : chatKeys) {
                try {
                    // 获取key的过期时间（秒）
                    Long ttlSeconds = redisTemplate.getExpire(key);
                    
                    if (ttlSeconds == null || ttlSeconds == -1) {
                        // -1 表示永久有效，需要删除
                        redisTemplate.delete(key);
                        deletedCount++;
                        log.debug("删除永久有效的key: {}", key);
                    } else if (ttlSeconds > 0 && ttlSeconds < TimeUnit.DAYS.toSeconds(7)) {
                        // TTL小于7天，即将过期，检查是否需要续期
                        String conversationId = key.substring(CHAT_PREFIX.length());
                        String[] parts = conversationId.split("_", 2);
                        
                        if (parts.length == 2) {
                            Long sessionId = Long.valueOf(parts[1]);
                            AiChatSession session = aiChatSessionMapper.selectById(sessionId);
                            
                            if (session == null) {
                                // 会话已删除，直接删除key
                                redisTemplate.delete(key);
                                deletedCount++;
                            } else {
                                // 会话仍存在，续期
                                redisTemplate.expire(key, 30, TimeUnit.DAYS);
                                renewedCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("深度清理key {} 时发生错误", key, e);
                }
            }
            
            log.info("AI对话历史深度清理完成，删除 {} 条，续期 {} 条", deletedCount, renewedCount);
            
        } catch (Exception e) {
            log.error("AI对话历史深度清理任务执行失败", e);
        }
    }
}
