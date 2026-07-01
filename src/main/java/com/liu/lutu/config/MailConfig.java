package com.liu.lutu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 */
@Configuration
public class MailConfig {

  @Value("${spring.email.host:${email.host:smtp.163.com}}")
  private String host;

  @Value("${spring.email.port:${email.port:465}}")
  private int port;

  @Value("${spring.email.username:${email.username:}}")
  private String username;

  @Value("${spring.email.password:${email.password:}}")
  private String password;

  @Value("${spring.email.nickname:${email.nickname:LuTu旅行}}")
  private String nickname;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);

    // 配置邮件属性
    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtps");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.ssl.enable", "true");
    props.put("mail.smtp.starttls.enable", "true");
    // props.put("mail.debug", "true"); // 调试时开启

    return mailSender;
  }
}
