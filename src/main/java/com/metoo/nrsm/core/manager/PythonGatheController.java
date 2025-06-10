package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.CredentialDTO;
import com.metoo.nrsm.core.service.ICredentialService;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.IVendorService;
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

import java.util.*;

@Slf4j
@RequestMapping("/admin/configuration")
@RestController
public class PythonGatheController {

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


    /*
    python main.py switch h3c 192.168.6.1 ssh 22 metoo metoo89745000 cur
     */
    @GetMapping ("/backup")
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

            return result;

        } catch (Exception e) {
            log.error("Backup failed for device {}: {}", device.getIp(), e.getMessage(), e);
            return BackupResult.error(device.getIp(), "Backup exception: " + e.getMessage());
        }
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
