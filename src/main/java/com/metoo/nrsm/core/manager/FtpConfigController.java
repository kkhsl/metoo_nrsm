package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.utils.AESUtils;
import com.metoo.nrsm.core.mapper.FtpConfigMapper;
import com.metoo.nrsm.core.service.impl.FtpConfigServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FtpConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/ftp")
public class FtpConfigController {

    @Autowired
    private FtpConfigServiceImpl ftpConfigService;

    @Autowired
    private FtpConfigMapper ftpConfigMapper;

    /**
     * 新增FTP配置
     */
    @PostMapping("/save")
    public Result save(@RequestBody FtpConfig dto) {
        Result ftpConfig = ftpConfigService.createFtpConfig(dto);
        return ftpConfig;
    }


    @GetMapping("/list")
    public Result getAllActive() {
        Result result = ftpConfigService.selectAllQuery();
        return result;
    }

    @DeleteMapping("/delete")
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public Result delete(@RequestParam String ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseUtil.badArgument("参数错误");
        }

        List<Long> checkIds = new ArrayList<>();
        for (String idStr : ids.split(",")) {
            try {
                checkIds.add(Long.parseLong(idStr.trim()));
            } catch (NumberFormatException e) {
                return ResponseUtil.badArgument("非法ID格式: " + idStr);
            }
        }

        List<FtpConfig> unitsToDelete = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long id : checkIds) {
            FtpConfig unit = ftpConfigMapper.selectById(Math.toIntExact(id));
            if (unit == null) {
                errors.add("ftp账号不存在: " + id);
                continue;
            }
            // 没有错误则添加待删除列表
            if (errors.isEmpty()) {
                unitsToDelete.add(unit);
            }
        }

        for (FtpConfig ftpConfig : unitsToDelete) {
            ftpConfig.setDeleteStatus(true);
            ftpConfigService.updateFtpConfig(ftpConfig);
        }

        if (!errors.isEmpty()) {
            return ResponseUtil.badArgument(String.join("; ", errors));
        }
        return ResponseUtil.ok("删除成功");
    }



    /**
     * 测试FTP配置连接是否成功
     * @param id FTP配置ID
     * @return 测试结果
     */
    @GetMapping("/test")
    public Result testFtpConnection(@RequestParam Integer id) {
        if (id == null || id <= 0) {
            return ResponseUtil.badArgument("参数错误");
        }

        FtpConfig config = ftpConfigMapper.selectById(id);
        if (config == null) {
            return ResponseUtil.badArgument("FTP配置不存在");
        }

        if (Boolean.TRUE.equals(config.getDeleteStatus())) {
            return ResponseUtil.badArgument("该FTP配置已被删除");
        }

        // 使用FTP客户端测试连接
        try {
            return testFtpConnectivity(config);
        } catch (Exception e) {
            return ResponseUtil.fail("FTP连接测试失败: " + e.getMessage());
        }
    }



    private Result testFtpConnectivity(FtpConfig config) throws Exception {
        FTPClient ftpClient = new FTPClient();
        boolean success = false;
        String errorMessage = "";
        String password = "";

        try {
            // 1. 设置连接参数
            ftpClient.setConnectTimeout(5000); // 5秒连接超时
            ftpClient.setDataTimeout(10000);   // 10秒数据传输超时

            // 2. 连接服务器
            ftpClient.connect(config.getFtpHost(), Integer.parseInt(config.getFtpPort()));

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                errorMessage = "服务器拒绝连接: " + ftpClient.getReplyString();
                return ResponseUtil.fail(errorMessage);
            }

            // 3. 登录认证
            if (!ftpClient.login(config.getUserName(), AESUtils.decrypt(config.getPassword()))) {
                errorMessage = "登录失败: " + ftpClient.getReplyString();
                return ResponseUtil.fail(errorMessage);
            }

            // 4. 设置文件传输模式
            ftpClient.enterLocalPassiveMode();  // 被动模式适合大部分情况
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 5. 测试列出目录（基本操作测试）
            String[] files = ftpClient.listNames(config.getFilePath());
            success = true;

            // 6. 断开连接
            ftpClient.logout();

            return ResponseUtil.ok(success ? "连接测试成功" : "连接测试失败");

        } catch (NumberFormatException e) {
            errorMessage = "端口格式错误: " + config.getFtpPort();
            return ResponseUtil.fail(errorMessage);
        } catch (IOException e) {
            errorMessage = "连接异常: " + e.getMessage();
            return ResponseUtil.fail(errorMessage);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                // 忽略断开连接的异常
            }
        }
    }



}