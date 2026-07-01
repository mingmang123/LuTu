package com.liu.lutu.memory;

import cn.hutool.core.collection.CollStreamUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisChatMemory implements ChatMemory {
    private static final String DEFAULT_PREFIX = "CHAT:";
    
    // 对话历史默认过期时间：30天
    private static final long DEFAULT_EXPIRE_DAYS = 30;

    private final String prefix;
    private final long expireDays;

    public RedisChatMemory() {
        this.prefix = DEFAULT_PREFIX;
        this.expireDays = DEFAULT_EXPIRE_DAYS;
    }

    public RedisChatMemory(String prefix, long expireDays) {
        this.prefix = prefix;
        this.expireDays = expireDays;
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages.isEmpty()) {
            return;
        }
        // 获取redis的key
        var redisKey = getKey(conversationId);
        // 获取redis的list操作对象
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        messages.forEach(message -> {
            //将message转换为json字符串存储到redis中
            listOps.rightPush(MessageUtil.toJson(message));
        });
        
        // 【关键】每次添加消息，重置过期时间（保持活跃会话不过期）
        listOps.expire(expireDays, TimeUnit.DAYS);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if(lastN <= 0){
            return List.of();
        }
        // 生成Redis键名用于存储会话消息
        var redisKey = this.getKey(conversationId);
        // 获取Redis列表操作对象
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        
        // 【关键】读取时也重置过期时间（活跃会话续期）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
            listOps.expire(expireDays, TimeUnit.DAYS);
        }
        
        // 从Redis列表中获取指定范围的元素（从第一个元素开始到lastN位置）
        var messages = listOps.range(0, lastN);
        // 将Redis返回的字符串列表转换为Message对象列表
        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
    }

    @Override
    public void clear(String conversationId) {
        var redisKey = getKey(conversationId);
        stringRedisTemplate.delete(redisKey);
    }
    
    /**
     * 获取所有对话key（用于定时清理）
     * @return 所有CHAT:开头的key集合
     */
    public Set<String> getAllConversationKeys() {
        return stringRedisTemplate.keys(prefix + "*");
    }
    
    /**
     * 获取key的剩余过期时间（天）
     * @param conversationId 对话ID
     * @return 剩余天数，-1表示永久有效，-2表示key不存在
     */
    public Long getExpireDays(String conversationId) {
        var redisKey = getKey(conversationId);
        return stringRedisTemplate.getExpire(redisKey, TimeUnit.DAYS);
    }
    
    /**
     * 删除指定对话的历史记录
     * @param conversationId 对话ID
     * @return 是否删除成功
     */
    public boolean deleteConversation(String conversationId) {
        var redisKey = getKey(conversationId);
        return Boolean.TRUE.equals(stringRedisTemplate.delete(redisKey));
    }

    // 获取redis的key
    private String getKey(String conversationId) {
        return prefix + conversationId;
    }
}
