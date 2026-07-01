package com.liu.lutu.service;

import com.liu.lutu.domain.dto.LoginDTO;
import com.liu.lutu.domain.dto.UpdatePasswordDTO;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.User;
import com.liu.lutu.domain.vo.TokenResponse;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-02
 */
public interface IUserService extends IService<User> {
  /**
   * 用户获取验证码
   * 
   * @param email 邮箱
   * @return 验证码
   */
  Result sendlogin(String email);

  /**
   * 用户登录验证（双Token版本）
   * 
   * @param loginDTO 登录信息
   * @return TokenResponse 包含Access Token和Refresh Token
   */
  Result<TokenResponse> captchalogin(LoginDTO loginDTO);

  /**
   * 获取用户信息
   * 
   * @return 用户信息
   */
  Result<User> getByUserId();

  /**
   * 用户密码登录（双Token版本）
   * 
   * @param loginDTO 登录信息
   * @return TokenResponse 包含Access Token和Refresh Token
   */
  Result<TokenResponse> loginByPassword(LoginDTO loginDTO);
  
  /**
   * 刷新Access Token
   * 
   * @param refreshToken 刷新令牌
   * @return 新的TokenResponse
   */
  Result<TokenResponse> refreshToken(String refreshToken);
  
  /**
   * 用户登出
   * 
   * @return 登出结果
   */
  Result logout();

  /**
   * 用户修改密码
   * 
   * @param updatePasswordDTO 密码信息
   * @return 修改结果
   */
  Result updatePassword(UpdatePasswordDTO updatePasswordDTO);

  /**
   * 上传头像
   * 
   * @param file 头像文件
   * @return 头像URL
   */
  Result<String> uploadAvatar(org.springframework.web.multipart.MultipartFile file);

  /**
   * 更新头像URL
   * 
   * @param avatarUrl 头像URL
   * @return 更新结果
   */
  Result updateAvatar(String avatarUrl);

  /**
   * 修改用户名
   * 
   * @param username 新用户名
   * @return 修改结果
   */
  Result updateUsername(String username);
}
