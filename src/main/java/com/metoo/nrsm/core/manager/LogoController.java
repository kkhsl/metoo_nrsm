package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.impl.LogoServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.WebSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @PostMapping("/uploadIco")
    public Result icoUpload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseUtil.error("请选择ico文件");
        }

        // 验证图片类型
        if (!isIcoFile(file)) {
            return ResponseUtil.error("仅支持ico文件");
        }

        String ico = logoService.uploadIco(file);
        return ResponseUtil.ok("ico更新成功");
    }

    private boolean isIcoFile(MultipartFile file) throws IOException {
        // 检查文件名是否以.ico结尾（不区分大小写）
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".ico")) {
            return false;
        }

        // 读取文件头的前8个字节
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            if (is.read(header) < 8) {
                return false; // 文件太小，不可能是有效的ICO
            }
        }

        // 验证是否为传统ICO格式：前4字节为 00 00 01 00
        if (header[0] == 0x00 && header[1] == 0x00 &&
                header[2] == 0x01 && header[3] == 0x00) {
            return true;
        }

        // 验证是否为PNG嵌入的ICO格式（前8字节匹配PNG签名）
        byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (Arrays.equals(header, pngSignature)) {
            return true;
        }

        return false;
    }




    @PostMapping("/save")
    public Result saveName(@RequestBody WebSet instance) throws IOException {
        return logoService.save(instance);
    }

    @GetMapping("/name")
    public Result list() {
        String name = logoService.getCurrentName();
        String logoUrl = logoService.getCurrentLogo()+"?t="+System.currentTimeMillis();
        String icoUrl = logoService.getCurrentIco()+"?t="+System.currentTimeMillis();
        Map map=new HashMap();
        map.put("name",name);
        map.put("logoUrl",logoUrl);
        map.put("icoUrl",icoUrl);
        return ResponseUtil.ok(map);
    }



}