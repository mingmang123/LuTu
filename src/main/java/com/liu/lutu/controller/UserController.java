package com.liu.lutu.controller;

import com.liu.lutu.domain.dto.LoginDTO;
import com.liu.lutu.domain.dto.UpdatePasswordDTO;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.User;
import com.liu.lutu.domain.vo.TokenResponse;
import com.liu.lutu.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author liu
 * @since 2026-04-02
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理")
public class UserController {
  private final IUserService userService;

  /**
   * 用户登录后查询用户信息来显示，后端通过token获取用户信息
   * 
   * @param
   * @return 用户信息
   */
  @Operation(summary = "用户查询", description = "用户查询")
  @GetMapping("/get")
  public Result<User> get() {
    return userService.getByUserId();
  }

  /**
   * 用户注册,返回验证码
   * 
   * @param email 邮箱
   * @return
   */
  @Operation(summary = "用户获取验证", description = "用户登录获取验证码")
  @GetMapping("/sendlogin")
  public Result sendlogin(String email) {
    return userService.sendlogin(email);
  }

  /**
   * 用户注册验证验证码，返回双Token
   * 
   * @param loginDTO 登录信息（支持rememberMe字段）
   * @return TokenResponse 包含Access Token和Refresh Token
   */
  @Operation(summary = "用户注册登录验证", description = "用户登录验证验证码，返回双Token")
  @PostMapping("/captchalogin")
  public Result<TokenResponse> captchalogin(@RequestBody LoginDTO loginDTO) {
    return userService.captchalogin(loginDTO);
  }

  /**
   * 用户修改密码
   * 
   * @param updatePasswordDTO 密码信息
   * @return
   */
  @Operation(summary = "用户修改密码", description = "用户修改密码")
  @PutMapping("/updatePassword")
  public Result updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
    return userService.updatePassword(updatePasswordDTO);
  }

  /**
   * 用户密码登录，返回双Token
   * 
   * @param loginDTO 登录信息（支持rememberMe字段）
   * @return TokenResponse 包含Access Token和Refresh Token
   */
  @Operation(summary = "用户登录", description = "用户密码登录，返回双Token")
  @PostMapping("/login")
  public Result<TokenResponse> loginwithpassword(@RequestBody LoginDTO loginDTO) {
    return userService.loginByPassword(loginDTO);
  }
  
  /**
   * 刷新Access Token（Token轮换机制）
   * 
   * @param refreshToken 刷新令牌（从Header获取）
   * @return 新的TokenResponse
   */
  @Operation(summary = "刷新Token", description = "使用Refresh Token换取新的双Token")
  @PostMapping("/refresh")
  public Result<TokenResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
    return userService.refreshToken(refreshToken);
  }
  
  /**
   * 用户登出
   * 
   * @return 登出结果
   */
  @Operation(summary = "用户登出", description = "用户登出，清除所有Token")
  @PostMapping("/logout")
  public Result logout() {
    return userService.logout();
  }

  /**
   * 上传头像
   * 
   * @param file 头像文件
   * @return 头像URL
   */
  @Operation(summary = "上传头像", description = "上传用户头像")
  @PostMapping("/avatar")
  public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
    return userService.uploadAvatar(file);
  }

  /**
   * 更新头像URL
   * 
   * @param avatarUrl 头像URL
   * @return
   */
  @Operation(summary = "更新头像", description = "更新用户头像URL")
  @PutMapping("/avatar")
  public Result updateAvatar(@RequestParam("avatarUrl") String avatarUrl) {
    return userService.updateAvatar(avatarUrl);
  }

  /**
   * 修改用户名
   * 
   * @param username 新用户名
   * @return
   */
  @Operation(summary = "修改用户名", description = "修改用户用户名")
  @PutMapping("/username")
  public Result updateUsername(@RequestParam("username") String username) {
    return userService.updateUsername(username);
  }

}
