package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.WebSetMapper;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.WebSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class LogoServiceImpl {

    @Value("${logo.upload.path}")
    private String uploadPath;

    @Value("${logo.name}")
    private String logoName;

    @Value("${sys.address}")
    private String ip;


    @Autowired
    private WebSetMapper webSetMapper;

    public String uploadLogo(MultipartFile file) throws IOException {
        // 1. 验证文件类型
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("仅支持PNG、JPG、JPEG格式的图片");
        }

        // 2. 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }



        // 3. 获取当前配置
        WebSet currentConfig = getCurrentConfig();
//        String oldLogoPath = currentConfig != null ? currentConfig.getLogoUrl() : null;
        String fileName = logoName + getFileExtension(file.getOriginalFilename());
        String oldLogoPath = uploadPath+"/"+fileName;

        // 7. 删除旧Logo文件（如果存在）
        if (oldLogoPath != null && !oldLogoPath.isEmpty()) {
            deleteOldLogoFile(oldLogoPath);
        }

        // 4. 生成唯一文件名
//        String fileName = "logo_" + System.currentTimeMillis() + getFileExtension(file.getOriginalFilename());
        fileName = logoName + getFileExtension(file.getOriginalFilename());
        String newLogoPath = ip+ fileName;

        // 5. 保存新文件
        Path targetPath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 6. 更新数据库
        if (currentConfig != null) {
            // 更新现有配置
            currentConfig.setLogoUrl(newLogoPath);
            webSetMapper.updateLogo(currentConfig.getId(), newLogoPath);
        } else {
            // 创建新配置
            WebSet newConfig = new WebSet();
            newConfig.setLogoUrl(newLogoPath);
            webSetMapper.insert(newConfig);
        }



        return newLogoPath;
    }

    public Result save(WebSet instance) throws IOException {
        WebSet currentConfig = getCurrentConfig();
        int i=0;
        if (instance.getName()!=null){
           String name = getCurrentName();
           if (name!=null){
               instance.setId(currentConfig.getId());
                i= webSetMapper.updateName(instance);
           }else {
                i = webSetMapper.insertName(instance);
           }
           if(i>0){
               return ResponseUtil.ok("保存成功");
           }else {
               return ResponseUtil.error("保存失败");
           }
       }else {
           return ResponseUtil.error("名称不能为空！");
       }
    }
    public String getCurrentName() {
        WebSet currentConfig = getCurrentConfig();
        return currentConfig != null ? currentConfig.getName() : null;
    }



    public String getCurrentLogo() {
        WebSet currentConfig = getCurrentConfig();
        return currentConfig != null ? currentConfig.getLogoUrl() : null;
    }

    private WebSet getCurrentConfig() {
        List<WebSet> configs = webSetMapper.getAllConfigs();
        return configs.isEmpty() ? null : configs.get(0);
    }

    private void deleteOldLogoFile(String oldLogoPath) {
        try {
            Path oldPath = Paths.get(oldLogoPath);
            if (Files.exists(oldPath)) {
                Files.delete(oldPath);
            }
        } catch (IOException e) {
            // 记录日志但不要中断主流程
            //System.err.println("删除旧Logo文件失败: " + e.getMessage());
        }
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/png") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg"));
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}