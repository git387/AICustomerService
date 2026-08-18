package com.king.aicustomerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务
 * 所有文件统一存放到 D:/workspace/uploads 下
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${app.upload.path}")
    private String uploadPath;

    /**
     * 启动时创建商品图和知识库目录
     */
    @PostConstruct
    public void initDirs() {
        try {
            Files.createDirectories(Paths.get(uploadPath, "products"));
            Files.createDirectories(Paths.get(uploadPath, "knowledge"));
            log.info("上传目录已就绪: {}", uploadPath);
        } catch (IOException e) {
            log.warn("创建上传目录失败: {}", e.getMessage());
        }
    }

    /**
     * 保存商品图片，返回可通过 /uploads 访问的相对路径
     */
    public String saveProductImage(MultipartFile file) {
        return save(file, "products");
    }

    /**
     * 保存知识库文件，返回磁盘绝对路径
     */
    public Path saveKnowledgeFile(MultipartFile file) {
        String stored = save(file, "knowledge");
        String filename = Paths.get(stored).getFileName().toString();
        return Paths.get(uploadPath, "knowledge", filename);
    }

    /**
     * 将文件保存到指定子目录
     *
     * @return 形如 /uploads/products/xxx.jpg 的访问路径
     */
    public String save(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot);
        }
        String filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path dir = Paths.get(uploadPath, subDir);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }
}
