package com.liu.lutu.config;

import com.liu.lutu.constants.constant;
import com.liu.lutu.memory.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SpringAiConfig {

    /**
     * 创建并返回一个ChatClient的Spring Bean实例。
     *
     * @param builder 用于构建ChatClient实例的构建者对象
     * @return 构建好的ChatClient实例
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 Advisor simpleLoggerAdvisor,
                                 Advisor messageChatMemoryAdvisor) {
        return builder
                //设置默认系统角色
                .defaultSystem(constant.SYSTEM_ROLE)
                // 设置默认的Advisor
                .defaultAdvisors(simpleLoggerAdvisor,messageChatMemoryAdvisor)
                .build();
    }


    /**
     * 创建并返回一个SimpleLoggerAdvisor的Spring Bean实例。 用法日志
     */
    @Bean
    public Advisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new RedisChatMemory();
    }

    /**
     * 基于Redis的会话记忆，聊天记忆整合到system message中实现多轮对话
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return new MessageChatMemoryAdvisor(chatMemory);
    }


}
