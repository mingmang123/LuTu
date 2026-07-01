package com.liu.lutu.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    // 邮箱
    private String  email;

    // 用户输入的验证码
    private String code;

    // 用户输入的密码
    private String password;
    
    // 是否记住我（延长Refresh Token有效期）
    private Boolean rememberMe;
}
