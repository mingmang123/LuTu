package com.liu.lutu.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云OSS服务接口
 */
public interface IOssService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param dir  目录
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String dir);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 生成临时上传凭证（用于前端直传）
     *
     * @param dir 目录
     * @return 上传凭证信息
     */
    java.util.Map<String, String> generateUploadToken(String dir);

}
