package com.liu.lutu.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.liu.lutu.config.OssConfig;
import com.liu.lutu.service.IOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 阿里云OSS服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements IOssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Override
    public String uploadFile(MultipartFile file, String dir) {
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = dir + "/" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" +
                    UUID.randomUUID().toString().replace("-", "") + suffix;

            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // 上传文件
            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream(), metadata);

            // 返回URL
            if (ossConfig.getCustomDomain() != null && !ossConfig.getCustomDomain().isEmpty()) {
                return ossConfig.getCustomDomain() + "/" + fileName;
            } else {
                return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + fileName;
            }
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName = extractFileName(fileUrl);
            if (fileName == null) {
                return false;
            }
            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return false;
        }
    }

    @Override
    public Map<String, String> generateUploadToken(String dir) {
        // 生成前端直传所需的参数
        String fileName = dir + "/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" +
                UUID.randomUUID().toString().replace("-", "") + "_${filename}";

        return Map.of(
                "accessId", ossConfig.getAccessKeyId(),
                "policy", generatePolicy(),
                "signature", generateSignature(fileName),
                "dir", dir,
                "host", "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint(),
                "fileName", fileName
        );
    }

    private String extractFileName(String fileUrl) {
        if (ossConfig.getCustomDomain() != null && !ossConfig.getCustomDomain().isEmpty()) {
            if (fileUrl.startsWith(ossConfig.getCustomDomain())) {
                return fileUrl.substring(ossConfig.getCustomDomain().length() + 1);
            }
        }
        String prefix = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        return null;
    }

    private String generatePolicy() {
        // 简化实现，实际应该生成Base64编码的POST policy
        return "";
    }

    private String generateSignature(String fileName) {
        // 简化实现，实际需要根据policy计算签名
        return "";
    }

}
