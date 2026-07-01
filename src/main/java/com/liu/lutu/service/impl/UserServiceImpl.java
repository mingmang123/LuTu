package com.liu.lutu.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.lutu.config.OssConfig;
import com.liu.lutu.domain.dto.LoginDTO;
import com.liu.lutu.domain.dto.UpdatePasswordDTO;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.User;
import com.liu.lutu.domain.vo.TokenResponse;
import com.liu.lutu.mapper.UserMapper;
import com.liu.lutu.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.util.EmailUtil;
import com.liu.lutu.util.JwtUtil;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
  private final EmailUtil emailUtil;
  private final JwtUtil jwtUtil;
  private final StringRedisTemplate redisTemplate;
  private final OSS ossClient;
  private final OssConfig ossConfig;
  
  // Access Token有效期：15分钟
  private static final long ACCESS_TOKEN_EXPIRE = 15 * 60;
  // Refresh Token有效期（普通）：7天
  private static final long REFRESH_TOKEN_EXPIRE_NORMAL = 7 * 24 * 60 * 60;
  // Refresh Token有效期（记住我）：30天
  private static final long REFRESH_TOKEN_EXPIRE_REMEMBER = 30 * 24 * 60 * 60;

  /**
   * 用户登录
   * 
   * @param email 邮箱
   * @return 验证码
   */
  @Override
  public Result sendlogin(String email) {
    // 验证邮箱格式
    if (!EmailUtil.isEmail(email)) {
      return Result.fail("邮箱格式错误");
    }
    // 生成验证码
    String VerificationCode = RandomUtil.randomString(6);
    log.info("验证码是:{}", VerificationCode);
    // 将验证码存入redis
    redisTemplate.opsForValue().set("user:" + email, VerificationCode, 3, TimeUnit.MINUTES);
    // 发送邮件
    emailUtil.sendEmail(email, VerificationCode);
    return Result.success();
  }

  /**
   * 用户登录验证（双Token版本）
   * 
   * @param loginDTO 登录信息
   * @return TokenResponse 包含Access Token和Refresh Token
   */
  @Override
  public Result<TokenResponse> captchalogin(LoginDTO loginDTO) {
    String email = loginDTO.getEmail();
    String code = loginDTO.getCode();
    Boolean rememberMe = loginDTO.getRememberMe();
    
    if (StrUtil.isBlank(email) || StrUtil.isBlank(code)) {
      return Result.fail("邮箱或验证码为空");
    }
    if (!EmailUtil.isEmail(email)) {
      return Result.fail("邮箱格式错误");
    }
    // 从redis中获取验证码
    String Code = redisTemplate.opsForValue().get("user:" + email);
    if (StrUtil.isBlank(Code)) {
      return Result.fail("验证码已过期");
    }
    if (!code.equals(Code)) {
      return Result.fail("验证码错误");
    }
    // 判断用户是否存在，如果存在，则返回用户信息，如果不存在，则创建用户，并返回用户信息
    User user = lambdaQuery().eq(User::getEmail, email).one();
    // 如果用户不存在，则创建用户
    if (user == null) {
      user = new User();
      user.setEmail(email);
      user.setStatus(1);
      user.setCreateTime(LocalDateTime.now());
      // 默认用户名为邮箱前5位
      user.setUsername(email.substring(0, 5));
      save(user);
    }
    
    // 生成双Token
    TokenResponse tokens = generateTokens(user.getId(), rememberMe != null && rememberMe);
    
    return Result.success(tokens);
  }

  /**
   * 获取用户信息
   * 
   * @return 用户信息
   */
  @Override
  public Result<User> getByUserId() {
    // 获取当前用户ID
    Long userId = ThreadlocalUtil.getUserId();
    // 根据用户ID查询用户信息
    User user = getById(userId);
    // 将密码置空
    user.setPassword(null);
    if (user == null) {
      return Result.fail("用户不存在");
    }
    return Result.success(user);
  }

  /**
   * 用户密码登录（双Token版本）
   * 
   * @param loginDTO 登录信息
   * @return TokenResponse 包含Access Token和Refresh Token
   */
  @Override
  public Result<TokenResponse> loginByPassword(LoginDTO loginDTO) {
    // 根据邮箱查询用户信息
    User one = lambdaQuery().eq(User::getEmail, loginDTO.getEmail()).one();
    if (one == null) {
      return Result.fail("用户不存在");
    }
    if (!one.getPassword().equals(loginDTO.getPassword())) {
      return Result.fail("密码错误");
    }
    
    Boolean rememberMe = loginDTO.getRememberMe();
    
    // 生成双Token
    TokenResponse tokens = generateTokens(one.getId(), rememberMe != null && rememberMe);
    
    return Result.success(tokens);
  }

  /**
   * 用户修改密码
   * 
   * @param updatePasswordDTO 密码信息
   * @return 修改结果
   */
  @Override
  public Result updatePassword(UpdatePasswordDTO updatePasswordDTO) {
    // 获取当前用户ID
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    // 查询用户信息
    User user = getById(userId);
    if (user == null) {
      return Result.fail("用户不存在");
    }

    // 验证原密码
    String oldPassword = updatePasswordDTO.getPassword();
    if (StrUtil.isBlank(oldPassword) || !oldPassword.equals(user.getPassword())) {
      return Result.fail("原密码错误");
    }

    // 验证新密码
    String newPassword = updatePasswordDTO.getNewPassword();
    if (StrUtil.isBlank(newPassword)) {
      return Result.fail("新密码不能为空");
    }
    if (newPassword.length() < 6) {
      return Result.fail("新密码长度不能小于6位");
    }

    // 更新密码
    user.setPassword(newPassword);
    updateById(user);

    // 删除Redis中的token，强制用户重新登录
    redisTemplate.delete("user:" + userId);

    return Result.success();
  }

  /**
   * 上传头像
   * 
   * @param file 头像文件
   * @return 头像URL
   */
  @Override
  public Result<String> uploadAvatar(org.springframework.web.multipart.MultipartFile file) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    if (file.isEmpty()) {
      return Result.fail("请选择文件");
    }

    // 检查文件类型
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return Result.fail("请上传图片文件");
    }

    // 检查文件大小（最大 5MB）
    if (file.getSize() > 5 * 1024 * 1024) {
      return Result.fail("图片大小不能超过 5MB");
    }

    try {
      // 先查询用户的旧头像
      User oldUser = getById(userId);
      String oldAvatar = oldUser != null ? oldUser.getAvatar() : null;

      // 生成文件名
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename != null && originalFilename.contains(".")
          ? originalFilename.substring(originalFilename.lastIndexOf("."))
          : ".jpg";
      String filename = "avatar/" + userId + "_" + System.currentTimeMillis() + extension;

      // 上传到 OSS
      byte[] bytes = file.getBytes();
      ossClient.putObject(ossConfig.getBucketName(), filename, new ByteArrayInputStream(bytes));

      // 返回访问路径
      String avatarUrl;
      if (StrUtil.isNotBlank(ossConfig.getCustomDomain())) {
        avatarUrl = ossConfig.getCustomDomain() + "/" + filename;
      } else {
        avatarUrl = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + filename;
      }

      // 更新用户头像
      User user = new User();
      user.setId(userId);
      user.setAvatar(avatarUrl);
      updateById(user);

      // 删除旧头像
      if (StrUtil.isNotBlank(oldAvatar) && oldAvatar.contains("avatar/")) {
        try {
          // 从URL中提取文件名
          String oldFilename = oldAvatar.substring(oldAvatar.indexOf("avatar/"));
          ossClient.deleteObject(ossConfig.getBucketName(), oldFilename);
          log.info("删除旧头像成功: {}", oldFilename);
        } catch (Exception e) {
          log.warn("删除旧头像失败: {}, 头像URL: {}", e.getMessage(), oldAvatar);
        }
      }

      return Result.success(avatarUrl);
    } catch (Exception e) {
      log.error("上传头像失败", e);
      return Result.fail("上传失败：" + e.getMessage());
    }
  }

  /**
   * 更新头像URL
   * 
   * @param avatarUrl 头像URL
   * @return 更新结果
   */
  @Override
  public Result updateAvatar(String avatarUrl) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    if (StrUtil.isBlank(avatarUrl)) {
      return Result.fail("头像URL不能为空");
    }

    User user = new User();
    user.setId(userId);
    user.setAvatar(avatarUrl);
    updateById(user);

    return Result.success();
  }

  /**
   * 修改用户名
   * 
   * @param username 新用户名
   * @return 修改结果
   */
  @Override
  public Result updateUsername(String username) {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }

    // 验证用户名
    if (StrUtil.isBlank(username)) {
      return Result.fail("用户名不能为空");
    }

    // 去除首尾空格
    username = username.trim();

    // 长度验证：2-20个字符
    if (username.length() < 2 || username.length() > 20) {
      return Result.fail("用户名长度必须在2-20个字符之间");
    }

    // 格式验证：只能包含中文、字母、数字、下划线
    if (!username.matches("^[\u4e00-\u9fa5a-zA-Z0-9_]+$")) {
      return Result.fail("用户名只能包含中文、字母、数字和下划线");
    }

    // 检查用户名是否已被使用
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, username)
        .ne(User::getId, userId);
    if (count(wrapper) > 0) {
      return Result.fail("该用户名已被使用");
    }

    // 更新用户名
    User user = new User();
    user.setId(userId);
    user.setUsername(username);
    updateById(user);

    return Result.success();
  }
  
  // ==================== 双Token认证相关方法 ====================
  
  /**
   * 生成双Token
   * 
   * @param userId 用户ID
   * @param rememberMe 是否记住我
   * @return TokenResponse
   */
  private TokenResponse generateTokens(Long userId, boolean rememberMe) {
    // 生成Token家族标识（用于关联Access和Refresh，检测重用攻击）
    String tokenFamily = IdUtil.fastSimpleUUID();
    
    // 确定Refresh Token有效期
    long refreshExpireSeconds = rememberMe ? REFRESH_TOKEN_EXPIRE_REMEMBER : REFRESH_TOKEN_EXPIRE_NORMAL;
    
    // 生成Access Token（15分钟）
    String accessToken = jwtUtil.createToken(userId, ACCESS_TOKEN_EXPIRE * 1000);
    
    // 生成Refresh Token（随机字符串）
    String refreshToken = IdUtil.fastSimpleUUID();
    
    long now = System.currentTimeMillis() / 1000;
    
    // 清除该用户旧的Token（实现单设备登录：新登录踢掉旧登录）
    clearUserTokens(userId);
    
    // Redis存储新的Token
    redisTemplate.opsForValue().set(
        "user:access:" + userId,
        accessToken,
        ACCESS_TOKEN_EXPIRE,
        TimeUnit.SECONDS
    );
    
    redisTemplate.opsForValue().set(
        "user:refresh:" + userId,
        refreshToken,
        refreshExpireSeconds,
        TimeUnit.SECONDS
    );
    
    redisTemplate.opsForValue().set(
        "user:token_family:" + userId,
        tokenFamily,
        refreshExpireSeconds,
        TimeUnit.SECONDS
    );
    
    // 反向映射：Refresh Token -> 用户ID:Token家族
    redisTemplate.opsForValue().set(
        "refresh:map:" + refreshToken,
        userId + ":" + tokenFamily,
        refreshExpireSeconds,
        TimeUnit.SECONDS
    );
    
    log.info("生成双Token成功，用户ID: {}, 记住我: {}", userId, rememberMe);
    
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessExpire(now + ACCESS_TOKEN_EXPIRE)
        .refreshExpire(now + refreshExpireSeconds)
        .rememberMe(rememberMe)
        .build();
  }
  
  /**
   * 刷新Access Token（Token轮换机制）
   * 
   * @param refreshToken 刷新令牌
   * @return 新的TokenResponse
   */
  @Override
  public Result<TokenResponse> refreshToken(String refreshToken) {
    if (StrUtil.isBlank(refreshToken)) {
      return Result.fail("Refresh Token不能为空");
    }
    
    // 1. 验证Refresh Token是否存在
    String tokenInfo = redisTemplate.opsForValue().get("refresh:map:" + refreshToken);
    if (StrUtil.isBlank(tokenInfo)) {
      log.warn("Refresh Token无效或已过期");
      return Result.fail("登录已过期，请重新登录");
    }
    
    // 2. 解析用户ID和Token家族
    String[] parts = tokenInfo.split(":");
    if (parts.length != 2) {
      return Result.fail("Token格式错误");
    }
    Long userId = Long.valueOf(parts[0]);
    String oldTokenFamily = parts[1];
    
    // 3. 【安全检测】检查Token家族是否一致（防止Refresh Token重用攻击）
    String currentTokenFamily = redisTemplate.opsForValue()
        .get("user:token_family:" + userId);
    
    if (!oldTokenFamily.equals(currentTokenFamily)) {
      // Token家族不一致！可能发生了Token重用攻击
      log.warn("检测到Refresh Token重用攻击，用户ID: {}", userId);
      
      // 清除该用户所有Token（强制重新登录）
      clearUserTokens(userId);
      
      return Result.fail("账号存在安全风险，请重新登录");
    }
    
    // 4. 获取当前Refresh Token的过期时间（保持记住我状态）
    Long refreshExpire = redisTemplate.getExpire("user:refresh:" + userId);
    boolean rememberMe = refreshExpire != null && 
        refreshExpire > REFRESH_TOKEN_EXPIRE_NORMAL;
    
    // 5. 执行Token轮换：生成新的双Token，删除旧的Refresh Token
    TokenResponse newTokens = rotateTokens(userId, rememberMe);
    
    // 删除旧的Refresh Token映射（确保旧Refresh Token只能用一次）
    redisTemplate.delete("refresh:map:" + refreshToken);
    
    log.info("Token刷新成功，用户ID: {}", userId);
    
    return Result.success(newTokens);
  }
  
  /**
   * Token轮换：生成新Token并删除旧Token
   * 
   * @param userId 用户ID
   * @param rememberMe 是否记住我
   * @return 新的TokenResponse
   */
  private TokenResponse rotateTokens(Long userId, boolean rememberMe) {
    // 生成新的Token家族（关键！每次轮换都换新家族）
    String newTokenFamily = IdUtil.fastSimpleUUID();
    
    // 确定Refresh Token有效期
    long refreshExpireSeconds = rememberMe ? REFRESH_TOKEN_EXPIRE_REMEMBER : REFRESH_TOKEN_EXPIRE_NORMAL;
    
    // 生成新的Access Token
    String newAccessToken = jwtUtil.createToken(userId, ACCESS_TOKEN_EXPIRE * 1000);
    String newRefreshToken = IdUtil.fastSimpleUUID();
    
    long now = System.currentTimeMillis() / 1000;
    
    // 更新Redis中的Token（覆盖旧Token）
    redisTemplate.opsForValue().set(
        "user:access:" + userId,
        newAccessToken,
        ACCESS_TOKEN_EXPIRE,
        TimeUnit.SECONDS
    );
    
    redisTemplate.opsForValue().set(
        "user:refresh:" + userId,
        newRefreshToken,
        refreshExpireSeconds,
        TimeUnit.SECONDS
    );
    
    redisTemplate.opsForValue().set(
        "user:token_family:" + userId,
        newTokenFamily,
        refreshExpireSeconds,
        TimeUnit.SECONDS
    );
    
    redisTemplate.opsForValue().set(
        "refresh:map:" + newRefreshToken,
        userId + ":" + newTokenFamily,
        refreshExpireSeconds,
        TimeUnit.SECONDS
    );
    
    return TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .accessExpire(now + ACCESS_TOKEN_EXPIRE)
        .refreshExpire(now + refreshExpireSeconds)
        .rememberMe(rememberMe)
        .build();
  }
  
  /**
   * 用户登出
   * 
   * @return 登出结果
   */
  @Override
  public Result logout() {
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      return Result.fail("用户未登录");
    }
    
    // 清除用户所有Token
    clearUserTokens(userId);
    
    log.info("用户登出成功，用户ID: {}", userId);
    
    return Result.success();
  }
  
  /**
   * 清除用户所有Token（用于登出、安全应急等场景）
   * 
   * @param userId 用户ID
   */
  private void clearUserTokens(Long userId) {
    // 获取旧的Refresh Token，删除反向映射
    String oldRefreshToken = redisTemplate.opsForValue()
        .get("user:refresh:" + userId);
    if (StrUtil.isNotBlank(oldRefreshToken)) {
      redisTemplate.delete("refresh:map:" + oldRefreshToken);
    }
    
    // 删除用户所有Token相关数据
    redisTemplate.delete("user:access:" + userId);
    redisTemplate.delete("user:refresh:" + userId);
    redisTemplate.delete("user:token_family:" + userId);
    
    log.debug("清除用户Token，用户ID: {}", userId);
  }
}
