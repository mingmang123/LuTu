package com.liu.lutu.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePasswordDTO implements Serializable {
    // 原密码
    private String password;

    // 新密码
    private String newPassword;
}
