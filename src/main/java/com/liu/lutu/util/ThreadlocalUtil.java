package com.liu.lutu.util;

import com.liu.lutu.domain.po.User;

public class ThreadlocalUtil {
    // 存储当前线程的用户信息
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();
    // 存储当前线程的会话ID（用于AI工具调用）
    private static final ThreadLocal<Long> SESSION_THREAD_LOCAL = new ThreadLocal<>();

    // 保存当前用户
    public static void set(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }
    // 获取当前用户ID
    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    // 保存当前会话ID
    public static void setSessionId(Long sessionId) {
        SESSION_THREAD_LOCAL.set(sessionId);
    }
    // 获取当前会话ID
    public static Long getSessionId() {
        return SESSION_THREAD_LOCAL.get();
    }

    // 清除（必须！防止内存泄漏）
    public static void remove() {
        USER_THREAD_LOCAL.remove();
        SESSION_THREAD_LOCAL.remove();
    }
}
