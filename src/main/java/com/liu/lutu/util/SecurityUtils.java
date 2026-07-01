package com.liu.lutu.util;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * 安全工具类
 * 提供XSS防护、输入验证等安全功能
 */
public class SecurityUtils {

    // 危险的HTML标签模式
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PROTOCOL = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PROTOCOL = Pattern.compile("data:", Pattern.CASE_INSENSITIVE);
    private static final Pattern IFRAME_PATTERN = Pattern.compile("<iframe", Pattern.CASE_INSENSITIVE);
    private static final Pattern OBJECT_PATTERN = Pattern.compile("<object", Pattern.CASE_INSENSITIVE);

    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    // 只允许字母、数字、下划线、中文的正则
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[\\w\\u4e00-\\u9fa5\\s\\-,.!?]+$");

    /**
     * XSS过滤 - 移除危险的HTML标签和属性
     *
     * @param input 输入字符串
     * @return 过滤后的字符串
     */
    public static String xssFilter(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }

        String result = input;
        // 移除script标签
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");
        // 移除事件处理器
        result = EVENT_HANDLER_PATTERN.matcher(result).replaceAll("");
        // 移除javascript协议
        result = JAVASCRIPT_PROTOCOL.matcher(result).replaceAll("");
        // 移除data协议
        result = DATA_PROTOCOL.matcher(result).replaceAll("");
        // 移除iframe
        result = IFRAME_PATTERN.matcher(result).replaceAll("&lt;iframe");
        // 移除object
        result = OBJECT_PATTERN.matcher(result).replaceAll("&lt;object");

        return result;
    }

    /**
     * HTML转义
     *
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeHtml(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * 验证手机号
     *
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isValidPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证字符串是否安全（不包含危险字符）
     *
     * @param input 输入字符串
     * @return 是否安全
     */
    public static boolean isSafeString(String input) {
        if (StrUtil.isBlank(input)) {
            return true;
        }
        return SAFE_STRING_PATTERN.matcher(input).matches();
    }

    /**
     * 截断字符串到指定长度
     *
     * @param input     输入字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    public static String truncate(String input, int maxLength) {
        if (StrUtil.isBlank(input)) {
            return input;
        }
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }

    /**
     * 验证URL参数名是否合法
     *
     * @param paramName 参数名
     * @return 是否合法
     */
    public static boolean isValidParamName(String paramName) {
        if (StrUtil.isBlank(paramName)) {
            return false;
        }
        // 只允许字母、数字、下划线和连字符
        return paramName.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * 清理SQL注入风险（基础防护）
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sqlInjectFilter(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }

        // 移除常见的SQL注入关键字
        String[] keywords = {"select", "insert", "update", "delete", "drop", "truncate", "union", "exec", "script"};
        String result = input.toLowerCase();

        for (String keyword : keywords) {
            result = result.replace(keyword, "");
        }

        // 移除特殊字符
        return result.replace("'", "").replace(";", "").replace("--", "").replace("/*", "").replace("*/", "");
    }

    /**
     * 综合安全处理 - 对输入进行完整的清理
     *
     * @param input 输入字符串
     * @return 安全处理后的字符串
     */
    public static String sanitize(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }

        // 1. XSS过滤
        String result = xssFilter(input);
        // 2. SQL注入过滤
        result = sqlInjectFilter(result);
        // 3. 截断到合理长度
        result = truncate(result, 1000);

        return result;
    }
}
