package com.liu.lutu.controller;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.service.IOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

  private final IOssService ossService;

  /**
   * 上传图片
   *
   * @param file 图片文件
   * @param dir  目录（可选，默认images）
   * @return 图片URL
   */
  @PostMapping("/upload")
  public Result<String> uploadImage(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "dir", defaultValue = "images") String dir) {

    if (file.isEmpty()) {
      return Result.fail("请选择要上传的文件");
    }

    // 检查文件类型
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return Result.fail("只能上传图片文件");
    }

    // 检查文件大小（最大10MB）
    if (file.getSize() > 10 * 1024 * 1024) {
      return Result.fail("文件大小不能超过10MB");
    }

    try {
      String url = ossService.uploadFile(file, dir);
      return Result.success(url);
    } catch (Exception e) {
      return Result.fail("上传失败: " + e.getMessage());
    }
  }

  /**
   * 批量上传图片
   *
   * @param files 图片文件数组
   * @param dir   目录
   * @return 图片URL列表
   */
  @PostMapping("/upload/batch")
  public Result<java.util.List<String>> uploadImages(
      @RequestParam("files") MultipartFile[] files,
      @RequestParam(value = "dir", defaultValue = "images") String dir) {

    if (files == null || files.length == 0) {
      return Result.fail("请选择要上传的文件");
    }

    java.util.List<String> urls = new java.util.ArrayList<>();
    for (MultipartFile file : files) {
      if (!file.isEmpty()) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/") && file.getSize() <= 10 * 1024 * 1024) {
          try {
            String url = ossService.uploadFile(file, dir);
            urls.add(url);
          } catch (Exception e) {
            // 记录错误但继续处理其他文件
          }
        }
      }
    }

    return Result.success(urls);
  }

  /**
   * 获取上传凭证（用于前端直传）
   *
   * @param dir 目录
   * @return 上传凭证
   */
  @GetMapping("/upload/token")
  public Result<Map<String, String>> getUploadToken(
      @RequestParam(value = "dir", defaultValue = "images") String dir) {
    Map<String, String> token = ossService.generateUploadToken(dir);
    return Result.success(token);
  }

  /**
   * 删除文件
   *
   * @param fileUrl 文件URL
   * @return 是否成功
   */
  @DeleteMapping("/delete")
  public Result<Boolean> deleteFile(@RequestParam("url") String fileUrl) {
    boolean success = ossService.deleteFile(fileUrl);
    return Result.success(success);
  }

}
