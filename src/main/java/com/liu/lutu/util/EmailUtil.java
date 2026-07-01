package com.liu.lutu.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 邮件工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtil {

  // 邮箱通用正则
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

  private final JavaMailSender mailSender;

  @Value("${spring.email.nickname:${email.nickname:途记}}")
  private String senderNickname;

  @Value("${spring.email.username:${email.username:l2874028140@163.com}}")
  private String senderEmail;

  /**
   * 验证邮箱是否合法
   */
  public static boolean isEmail(String email) {
    if (email == null) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * 发送验证码邮件
   *
   * @param email 收件人邮箱
   * @param code  验证码
   */
  public void sendEmail(String email, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    // 设置发件人（带昵称）
    String fromAddress = senderNickname + " <" + senderEmail + ">";
    message.setFrom(fromAddress);
    message.setTo(email);
    message.setSubject("验证码");
    message.setText("您的验证码是：" + code + "，有效期3分钟，请勿泄露给他人。");

    try {
      mailSender.send(message);
      log.info("邮件发送成功，收件人：{}，验证码：{}", email, code);
    } catch (Exception e) {
      log.error("邮件发送失败，收件人：{}，原因：{}", email, e.getMessage(), e);
      throw new RuntimeException("邮件发送失败", e);
    }
  }
}
