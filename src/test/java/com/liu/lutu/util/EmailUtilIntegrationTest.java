package com.liu.lutu.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 邮件发送集成测试
 * 使用真实的邮件服务器配置发送邮件
 */
@SpringBootTest
class EmailUtilIntegrationTest {

    @Autowired
    private EmailUtil emailUtil;

    @Test
    void sendEmail_RealServer() {
        // 真实发送邮件
        assertDoesNotThrow(() -> {
            emailUtil.sendEmail("2874028140@qq.com", "111111");
        });

        System.out.println("邮件发送成功，请检查收件箱！");
    }
}
