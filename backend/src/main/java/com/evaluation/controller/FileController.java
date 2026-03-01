package com.evaluation.controller;

import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Use absolute path based on project working directory
            String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            File dest = new File(uploadDir, newFilename);
            file.transferTo(dest.getCanonicalFile());

            // Return file URL path
            String fileUrl = "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
