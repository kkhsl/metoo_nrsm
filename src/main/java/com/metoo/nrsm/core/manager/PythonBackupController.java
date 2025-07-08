package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.CredentialDTO;
import com.metoo.nrsm.core.dto.DeviceConfigDTO;
import com.metoo.nrsm.core.mapper.DeviceConfigMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/admin/configuration")
@RestController
public class PythonBackupController {

    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Autowired
    private INetworkElementService networkElementService;

    @Autowired
    private ICredentialService credentialService;

    @Autowired
    private IVendorService vendorService;

    @Autowired
    private IDeviceTypeService deviceTypeService;

    @Autowired
    private IDeviceConfigService deviceConfigService;

    @Autowired
    private DeviceConfigMapper deviceConfigMapper;


    @GetMapping("/select")
    private Result list(String ip) {
        DeviceConfigDTO instance = new DeviceConfigDTO();
        if (ip != null) {
            instance.setName(ip);
        }
        Page<DeviceConfig> page = deviceConfigService.selectAll(instance);
        if (page != null) {
            return ResponseUtil.ok(page);
        } else {
            return ResponseUtil.ok("该设备无备份配置");
        }
    }


    @DeleteMapping("/delete")
    public Result deleteBackups(@RequestParam String ids) {
        try {
            // 将逗号分隔的ID列表转为Long类型集合
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // 获取要删除的备份配置信息
            List<DeviceConfig> configs = deviceConfigMapper.selectByIds(idList);

            // 删除数据库记录
            int deletedCount = deviceConfigMapper.deleteByIds(idList);

            // 删除对应的备份文件
            int filesDeleted = deleteBackupFiles(configs);

            if (deletedCount == filesDeleted) {
                return ResponseUtil.ok("删除成功");
            } else {
                return ResponseUtil.error("删除失败");
            }
        } catch (Exception e) {
            log.error("Failed to delete backups", e);
            return ResponseUtil.fail("Failed to delete backups: " + e.getMessage());
        }
    }


    @GetMapping("/comparison")
    public Result getConfigurationsForComparison(
            @RequestParam Long id1,
            @RequestParam Long id2) {

        try {
            // 查询第一条配置
            DeviceConfig config1 = deviceConfigMapper.selectById(id1);
            if (config1 == null) {
                return ResponseUtil.fail("Configuration with ID " + id1 + " not found");
            }

            // 查询第二条配置
            DeviceConfig config2 = deviceConfigMapper.selectById(id2);
            if (config2 == null) {
                return ResponseUtil.fail("Configuration with ID " + id2 + " not found");
            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("config1", mapConfig(config1));
            result.put("config2", mapConfig(config2));

            return ResponseUtil.ok(result);

        } catch (Exception e) {
            log.error("Failed to get configurations for comparison", e);
            return ResponseUtil.fail("Failed to get configurations: " + e.getMessage());
        }
    }

    private Map<String, Object> mapConfig(DeviceConfig config) {
        Map<String, Object> configMap = new HashMap<>();
//        configMap.put("id", config.getId());
        configMap.put("name", config.getName());
        configMap.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(config.getTime()));
//        configMap.put("type", getBackupTypeName(config.getType()));
        configMap.put("content", config.getContent());
        return configMap;
    }

/*    private String getBackupTypeName(Integer type) {
        if (type == null) return "Unknown";
        switch (type) {
            case 1: return "Manual Backup";
            case 2: return "Auto Backup";
            default: return "Unknown Type (" + type + ")";
        }
    }*/


    /**
     * 删除备份文件
     */
    private int deleteBackupFiles(List<DeviceConfig> configs) {
        String backupStoragePath = Global.backupStoragePath;
        File backupDir = new File(backupStoragePath);
        int filesDeleted = 0;

        for (DeviceConfig config : configs) {
            // 构建文件名 (name.cfg)
            String fileName = config.getName() + ".cfg";
            File file = new File(backupDir, fileName);

            try {
                if (file.exists() && file.delete()) {
                    filesDeleted++;
                    log.info("Deleted backup file: {}", file.getAbsolutePath());
                } else if (!file.exists()) {
                    log.warn("Backup file not found: {}", file.getAbsolutePath());
                } else {
                    log.error("Failed to delete backup file: {}", file.getAbsolutePath());
                }
            } catch (SecurityException e) {
                log.error("Security exception when deleting file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        }

        return filesDeleted;
    }


    /*
    python main.py switch h3c 192.168.6.1 ssh 22 metoo metoo89745000 cur
     */
    @GetMapping("/backup")
    private Result gather(String ips) {
        // 初始化结果集合
        Map<String, BackupResult> results = new LinkedHashMap<>();

        try {
            // 获取要备份的设备列表
            List<NetworkElement> devicesToBackup;
            if (ips != null && !ips.isEmpty()) {
                List<String> ipList = Arrays.asList(ips.split(","));
                log.info("Received backup request for specific IPs: {}", ipList);
                devicesToBackup = networkElementService.selectConditionByIpQuery(ipList);
            } else {
                log.info("Received backup request for all devices");
                devicesToBackup = networkElementService.selectConditionByIpQuery(null);
            }

            if (CollectionUtils.isEmpty(devicesToBackup)) {
                log.warn("No devices found matching criteria");
                return ResponseUtil.fail("No devices found to backup");
            }

            log.info("Starting backup for {} devices", devicesToBackup.size());

            // 批量处理设备备份
            for (NetworkElement device : devicesToBackup) {
                BackupResult result = backupDevice(device);
                results.put(device.getIp(), result);

                // 添加简短的日志
                if (result.isSuccess()) {
                    log.info("Backup SUCCESS for {}: {}", device.getIp(), result.getMessage());
                } else {
                    log.error("Backup FAILED for {}: {}", device.getIp(), result.getMessage());
                }
            }

            // 生成汇总报告
            Map<String, Object> summary = generateBackupSummary(results);

            return ResponseUtil.ok(summary);

        } catch (Exception e) {
            log.error("Backup processing failed", e);
            return ResponseUtil.fail("Backup processing error: " + e.getMessage());
        }
    }

    /**
     * 备份单个设备
     */
    private BackupResult backupDevice(NetworkElement device) {
        try {
            // 验证凭证
            if (device.getCredentialId() == null) {
                return BackupResult.error(device.getIp(), "Credential is required");
            }

            CredentialDTO credentialDTO = new CredentialDTO();
            credentialDTO.setCredentialId(String.valueOf(device.getCredentialId()));

            // 获取凭证信息
            List<Credential> credentials = credentialService.selectObjByIdQuery(credentialDTO);
            if (CollectionUtils.isEmpty(credentials)) {
                return BackupResult.error(device.getIp(), "Credential not found for ID: " + device.getCredentialId());
            }
            Credential credential = credentials.get(0);

            // 构建脚本参数
            PythonScriptParams params = buildScriptParams(device, credential);

            // 执行备份脚本
            String execResult = executeBackupScript(params);

            // 解析执行结果
            BackupResult result = parseBackupResult(execResult);
            result.setDeviceIp(device.getIp());

            // 如果备份成功，处理备份文件
            if (result.isSuccess()) {
                try {
                    saveBackupToDatabase(device);
                } catch (Exception e) {
                    log.error("Backup file save failed for {}: {}", device.getIp(), e.getMessage(), e);
                    result.setMessage(result.getMessage() +
                            " But failed to save to database: " + e.getMessage());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Backup failed for device {}: {}", device.getIp(), e.getMessage(), e);
            return BackupResult.error(device.getIp(), "Backup exception: " + e.getMessage());
        }
    }

    /**
     * 保存备份文件到数据库
     */
    private void saveBackupToDatabase(NetworkElement device) throws Exception {
        String backupStoragePath = Global.backupStoragePath;
        File backupDir = new File(backupStoragePath);

        // 正则表达式匹配文件名
        String pattern = device.getIp() + "_\\d{4}[-:]\\d{2}[-:]\\d{2}[-_:]\\d{2}[-:]\\d{2}[-:]\\d{2}\\.cfg";
        Pattern fileNamePattern = Pattern.compile(pattern);

        // 查找最新备份文件
        File latestFile = null;
        long lastModified = Long.MIN_VALUE;

        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] files = backupDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && fileNamePattern.matcher(file.getName()).matches()) {
                        if (file.lastModified() > lastModified) {
                            lastModified = file.lastModified();
                            latestFile = file;
                        }
                    }
                }
            }
        }

        if (latestFile == null) {
            throw new FileNotFoundException("No backup file found for " + device.getIp());
        }

        // 解析文件名和备份时间
        String fileName = latestFile.getName();
        String filePrefix = fileName.substring(0, fileName.lastIndexOf('.'));

        // 从文件名中提取时间字符串
        String timePart = filePrefix.substring(filePrefix.indexOf('_') + 1);

        // 处理多种可能的时间分隔符（-或:）
        String normalizedTime = timePart
                .replace("_", " ")  // 替换下划线为空格
                .replaceAll("[-:]", "-");  // 统一分隔符为短横线

        // 创建多个日期格式尝试
        SimpleDateFormat[] possibleFormats = {
                new SimpleDateFormat("yyyy-MM-dd HH-mm-ss"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                new SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        };

        Date backupTime = null;
        Exception lastException = null;

        // 尝试多种日期格式解析
        for (SimpleDateFormat format : possibleFormats) {
            try {
                backupTime = format.parse(normalizedTime);
                break; // 解析成功则退出循环
            } catch (ParseException e) {
                lastException = e;
            }
        }

        // 如果所有格式都失败，抛出异常
        if (backupTime == null) {
            throw new ParseException("Could not parse date: " + normalizedTime, 0);
        }

        // 创建标准日期格式用于显示
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 读取文件内容
        String content = new String(Files.readAllBytes(latestFile.toPath()), StandardCharsets.UTF_8);

        // 创建配置对象
        DeviceConfig config = new DeviceConfig();
        config.setName(filePrefix);
        config.setTime(backupTime);
        config.setType(1); // 1 表示备份类型
        config.setContent(content);

        // 保存到数据库
        deviceConfigMapper.insertDeviceConfig(config);

        log.info("Backup file saved to database for {}: {}", device.getIp(), filePrefix);
        log.debug("Saved config: name={}, time={}, size={} bytes",
                filePrefix, dbDateFormat.format(backupTime), content.length());
    }

    /**
     * 构建脚本参数
     */
    private PythonScriptParams buildScriptParams(NetworkElement device, Credential credential) {
        PythonScriptParams params = new PythonScriptParams();

        // 设置设备类型（英文）
        DeviceType deviceType = deviceTypeService.selectObjById(device.getDeviceTypeId());
        params.setCommand(Optional.ofNullable(deviceType)
                .map(DeviceType::getNameEn)
                .orElse("switch"));

        // 设置厂商（英文小写）
        Vendor vendor = vendorService.selectObjById(device.getVendorId());
        params.setBrand(Optional.ofNullable(vendor)
                .map(Vendor::getNameEn)
                .map(String::toLowerCase)
                .orElse("generic"));

        // 设置IP地址
        params.setIp(device.getIp());

        // 设置协议和端口
        if (device.getConnectType() == 0) {
            params.setProtocol("ssh");
            params.setPort(Optional.ofNullable(device.getPortSSH()).orElse(22));
        } else {
            params.setProtocol("telnet");
            params.setPort(Optional.ofNullable(device.getPortTelnet()).orElse(23));
        }

        // 设置凭证信息
        params.setUsername(credential.getLoginName());
        params.setPassword(credential.getLoginPassword());
        params.setOption("cur");

        return params;
    }

    /**
     * 执行备份脚本
     */
    private String executeBackupScript(PythonScriptParams params) {
        String path = Global.BKPATH + "main.py";
        String[] scriptParams = {
                params.getCommand(),
                params.getBrand(),
                params.getIp(),
                params.getProtocol(),
                String.valueOf(params.getPort()),
                params.getUsername(),
                params.getPassword(),
                params.getOption()
        };

        log.debug("Executing backup script for {} with params: {}", params.getIp(), Arrays.toString(scriptParams));
        return pythonExecUtils.execPy(path, scriptParams);
    }

    /**
     * 解析备份结果
     */
    private BackupResult parseBackupResult(String execResult) {
        if (execResult.contains("true")) {
            return BackupResult.success("Configuration backup successful");
        }

        if (execResult.contains("false")) {
            return BackupResult.error("Configuration backup failed: " + execResult);
        }

        // 尝试解析其他可能的错误信息
        if (execResult.contains("Connection failed") || execResult.contains("timeout")) {
            return BackupResult.error("Device connection failed");
        }

        if (execResult.contains("Authentication failed")) {
            return BackupResult.error("Authentication failed");
        }

        return BackupResult.error("Unexpected backup result: " + execResult);
    }

    /**
     * 生成备份汇总报告
     */
    private Map<String, Object> generateBackupSummary(Map<String, BackupResult> results) {
        int successCount = 0;
        int failureCount = 0;

        for (BackupResult result : results.values()) {
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalDevices", results.size());
        summary.put("successfulBackups", successCount);
        summary.put("failedBackups", failureCount);
        summary.put("successRate", successCount * 100.0 / results.size() + "%");
        summary.put("detailedResults", results);

        return summary;
    }

    /**
     * 备份结果对象
     */
    @Data
    @Accessors(chain = true)
    private static class BackupResult {
        private boolean success;
        private String deviceIp;
        private String message;
        private Date timestamp = new Date();

        public static BackupResult success(String message) {
            BackupResult result = new BackupResult();
            result.success = true;
            result.message = message;
            return result;
        }

        public static BackupResult error(String message) {
            BackupResult result = new BackupResult();
            result.success = false;
            result.message = message;
            return result;
        }

        public static BackupResult error(String deviceIp, String message) {
            return error(message).setDeviceIp(deviceIp);
        }
    }


}
