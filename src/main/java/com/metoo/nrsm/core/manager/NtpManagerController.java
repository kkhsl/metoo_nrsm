package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.INtpService;
import com.metoo.nrsm.core.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RequestMapping("/admin/ntp")
@RestController
public class NtpManagerController {

    @Autowired
    private INtpService ntpService;


    @Value("${ssh.hostname}")
    private String host;
    @Value("${ssh.port}")
    private int port = 22;
    @Value("${ssh.username}")
    private String username;
    @Value("${ssh.password}")
    private String password;



    @GetMapping("/select")
    public Result selectTime() throws Exception {
        Map<String, List<String>> map = ntpService.select();
        boolean status1 = ntpService.status();
        // 获取当前服务器时间（指定时区，例如：上海时区）
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        // 定义时间格式（例如：yyyy-MM-dd HH:mm:ss）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = now.format(formatter);
        // 将时间添加到 Map 中
        map.put("time", Collections.singletonList(formattedTime));
        map.put("status1", Collections.singletonList(String.valueOf(status1)));
        return ResponseUtil.ok(map);
    }

    @GetMapping("/openTime")
    public Result openTime(Boolean flag) throws Exception {
        boolean open = ntpService.open(flag);
        if (flag) {
            if (open){
                return ResponseUtil.ok("打开成功");
            }else {
                return ResponseUtil.error("打开失败");
            }
        }else {
            if (open){
                return ResponseUtil.error("关闭失败");
            }else {
                return ResponseUtil.ok("关闭成功");
            }
        }
    }

    @GetMapping("/saveTime")
    public Result saveTime(String instance) throws Exception {
        boolean flag = ntpService.saveTime(instance);
        if (flag){
            if (ntpService.env()){
                return ResponseUtil.ok("启动成功");
            }else {
                return ResponseUtil.error("启动失败");
            }
        }else {
            return ResponseUtil.ok("保存失败");
        }
    }

    @GetMapping("/openNtp")
    public Result startNtp(Boolean flag) throws Exception {
        boolean open = ntpService.openNtp(flag);
        if (flag){
            if (open) {
                if (ntpService.env()){
                    return ResponseUtil.ok("打开成功");
                }else {
                    return ResponseUtil.error("启动失败");
                }
            }else {
                return ResponseUtil.error("打开失败");
            }
        }else {
            if (open) {
                if (ntpService.env()){
                    return ResponseUtil.ok("关闭成功");
                }else {
                    return ResponseUtil.error("启动失败");
                }
            }else {
                return ResponseUtil.error("关闭失败");
            }
        }
    }

    @PostMapping("/saveNtp")
    public Result saveNtp(@RequestBody List<String> instance) throws Exception {
        boolean flag = ntpService.saveNtp(instance);
        if (flag){
            if (ntpService.env()){
                return ResponseUtil.ok("启动成功");
            }else {
                return ResponseUtil.error("启动失败");
            }
        }else {
            return ResponseUtil.error("保存失败");
        }
    }
}
