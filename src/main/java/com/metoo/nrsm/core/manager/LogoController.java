package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.impl.LogoServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.WebSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/webSet")
public class LogoController {



    @Autowired
    private LogoServiceImpl logoService;

    @PostMapping("/upload")
    public Result uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseUtil.error("请选择Logo文件");
        }

        // 验证图片类型
        if (!isImageFile(file)) {
            return ResponseUtil.error("仅支持图片文件");
        }

        String logoUrl = logoService.uploadLogo(file);
        return ResponseUtil.ok("Logo更新成功");
    }

    @GetMapping
    public Result getLogo() {
        String logoUrl = logoService.getCurrentLogo();
        return ResponseUtil.ok(logoUrl);
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }


    @PostMapping("/save")
    public Result saveName(@RequestBody WebSet instance) throws IOException {
        return logoService.save(instance);
    }

    @GetMapping("/name")
    public Result list() {
        String name = logoService.getCurrentName();
        return ResponseUtil.ok(name);
    }



}