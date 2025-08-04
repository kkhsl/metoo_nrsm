package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.LicenseDto;
import com.metoo.nrsm.core.manager.utils.LicenseUtils;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.network.networkconfig.test.checkProcessStatus;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.utils.license.LicenseTools;
import com.metoo.nrsm.core.vo.DiskVO;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.License;
import com.metoo.nrsm.entity.PingIpConfig;
import com.metoo.nrsm.entity.Res;
import com.metoo.nrsm.entity.SystemUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/license")
public class LicenseManagerController {

    private ILicenseService licenseService;
    private LicenseTools licenseTools;
    private IDiskService diskService;
    private ISystemUsageService systemUsageService;
    private IUnboundService unboundService;
    private IPingIpConfigService pingIpConfigService;
    private IResService resService;

    @Autowired
    public LicenseManagerController(ILicenseService licenseService,
                                    LicenseTools licenseTools,
                                    IDiskService diskService,
                                    ISystemUsageService systemUsageService,
                                    IUnboundService unboundService,
                                    IPingIpConfigService pingIpConfigService,
                                    IResService resService) {
        this.licenseService = licenseService;
        this.licenseTools = licenseTools;
        this.diskService = diskService;
        this.systemUsageService = systemUsageService;
        this.unboundService = unboundService;
        this.pingIpConfigService = pingIpConfigService;
        this.resService = resService;
    }

    @Value("${api.url}")
    private String apiUrl;

    @RequestMapping("/all")
    @Scheduled(cron = "0 */10 * * * ?")
    public Result hourlyReport() throws Exception {
        Map<String, Object> reportData = collectSystemInfo();
        String result = sendToPlatform(reportData);
        return ResponseUtil.ok(result);
    }

    public Map<String, Object> collectSystemInfo() throws Exception {
        Map<String, Object> reportData = new LinkedHashMap<>();

        // 1. 收集基础信息
        Map<String, Object> baseInfo = new LinkedHashMap<>();
        License obj = licenseService.query().get(0);
        String sn = SystemInfoUtils.getSerialNumber();
        String licenseInfo = AesEncryptUtils.decrypt(obj.getLicense());
        LicenseVo license = JSONObject.parseObject(licenseInfo, LicenseVo.class);
        LicenseUtils.calculateLicenseDays(license);
        Map<String, Object> licensesInfo = new LinkedHashMap<>();
        licensesInfo.put("probe",license.isLicenseProbe());


        baseInfo.put("sn", sn);
        baseInfo.put("unit", license.getUnitName());
        baseInfo.put("version", license.getLicenseVersion());
        baseInfo.put("licenseModule", licensesInfo);

        reportData.put("baseInfo", baseInfo);

        // 2. 收集资源利用率
        Map<String, Object> resourceUsage = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -61);
        Date startTime = calendar.getTime();
        calendar.add(Calendar.SECOND, +61);
        Date endTime = calendar.getTime();
        Map map=new HashMap();
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        // CPU和内存 (取最新记录)
        List<SystemUsage> usages = systemUsageService.selectObjByMap(map);
        if (!usages.isEmpty()) {
            SystemUsage latest = usages.get(0);
            resourceUsage.put("cpuUsage", latest.getCpu_usage());
            resourceUsage.put("memUsage", latest.getMem_usage());
        }else {
            resourceUsage.put("cpuUsage", "N/A");
            resourceUsage.put("memUsage", "N/A");
        }

        // 磁盘空间
        DiskVO disk = diskService.getRootDisk();
        Map<String, Object> diskInfo = new LinkedHashMap<>();
        diskInfo.put("total", disk.getTotalSpaceFormatted());
        diskInfo.put("free", disk.getFreeSpaceFormatted());
        diskInfo.put("used", disk.getUsableSpaceFormatted());
        // 将格式化字符串转换为数字用于计算
        double utilization = calculateDiskUtilization(
                disk.getTotalSpaceFormatted(),
                disk.getUsableSpaceFormatted()
        );
        diskInfo.put("utilization", String.format("%.2f%%", utilization));
        resourceUsage.put("disk", diskInfo);

        reportData.put("resourceUsage", resourceUsage);

        // 3. 收集进程状态
        Map<String, Object> processStatus = new LinkedHashMap<>();
        processStatus.put("dhcpd", checkProcessStatus("dhcpd"));
        processStatus.put("dhcpd6", checkProcessStatus("dhcpd6"));
        processStatus.put("dns", unboundService.status());

        PingIpConfig pingConfig = pingIpConfigService.selectOneObj();
        processStatus.put("checkaliveip", pingConfig != null && pingConfig.isEnabled());

        reportData.put("processStatus", processStatus);

        return reportData;
    }

    private boolean checkProcessStatus(String processName) {
        String status = checkProcessStatus.checkProcessStatus(processName);
        return (status != null && status.equalsIgnoreCase("true"));
    }

    // 发送数据到平台接口
    private String sendToPlatform(Map<String, Object> reportData) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 1. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 2. 转换JSON数据
            String jsonData = JSON.toJSONString(reportData);

            // 3. 创建请求实体
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

            // 4. 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("平台接口上报失败: {}", e.getMessage());
            // 记录更详细的错误信息
            if (e instanceof RestClientResponseException) {
                RestClientResponseException rc = (RestClientResponseException) e;
                log.error("平台返回状态码: {}, 响应内容: {}",
                        rc.getRawStatusCode(), rc.getResponseBodyAsString());
            }
            return null;
        }
    }

    /**
     * 计算磁盘利用率（基于格式化的字符串）
     * @param totalStr 总空间（如"56.88 GB"）
     * @param usedStr 已用空间（如"26.62 GB"）
     * @return 利用率百分比（如46.8）
     */
    private double calculateDiskUtilization(String totalStr, String usedStr) {
        try {
            // 解析空间值（转换为GB单位）
            double totalGB = parseSpaceValue(totalStr);
            double usedGB = parseSpaceValue(usedStr);

            if (totalGB > 0) {
                return (usedGB / totalGB) * 100;
            }
            return 0.0;
        } catch (Exception e) {
            return -1.0; // 计算失败标记
        }
    }

    /**
     * 将格式化的空间字符串转换为GB单位的数字
     * 支持GB/MB/KB/B等单位
     */
    private double parseSpaceValue(String spaceStr) {
        // 分割数值和单位（如["56.88", "GB"]）
        String[] parts = spaceStr.split(" ");
        if (parts.length < 2) return 0.0;

        double value = Double.parseDouble(parts[0]);
        String unit = parts[1].toUpperCase();

        // 转换为GB单位
        switch (unit) {
            case "TB": return value * 1024;
            case "GB": return value;
            case "MB": return value / 1024;
            case "KB": return value / (1024 * 1024);
            case "B": return value / (1024 * 1024 * 1024);
            default: return value; // 默认为GB
        }
    }

    @RequestMapping("/systemInfo")
    public Object systemInfo() {
        try {
            String sn = SystemInfoUtils.getSerialNumber();
            return ResponseUtil.ok(sn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping(value = "/query")
    public Object query() {
        License obj = this.licenseService.query().get(0);
        String uuid = SystemInfoUtils.getSerialNumber();
        if (!uuid.equals(obj.getSystemSN())) {
            return ResponseUtil.error(413, "未授权设备");
        }
        try {
            String licenseInfo = AesEncryptUtils.decrypt(obj.getLicense());
            LicenseVo licenseVo = JSONObject.parseObject(licenseInfo, LicenseVo.class);
            LicenseUtils.calculateLicenseDays(licenseVo);
            if(licenseVo.getVersionType() == 2 || licenseVo.getVersionType() == 4){
                licenseVo.setLicenseProbe(true);
            }
            return ResponseUtil.ok(licenseVo);
        } catch (Exception e) {
            // 使用日志记录异常信息
            return ResponseUtil.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统授权码
     *
     * @param license
     * @return
     */
    @PutMapping("update")
    public Object license(@RequestBody Map<String, Object> license) {

        String uuid = SystemInfoUtils.getSerialNumber();

        String code = license.get("license").toString();

        boolean isValid = this.licenseTools.verifySN(uuid, code);

        if (isValid) {

            List<License> existingLicenses = this.licenseService.query();
            if (!existingLicenses.isEmpty()) {
                return updateExistingLicense(existingLicenses.get(0), code, uuid);
            } else {
                return createNewLicense(code, uuid);
            }
        }
        return ResponseUtil.error("非法授权码");
    }

    // 解析授权中PermissionCode
    public void updatePermission(List<String> permissionList){
        if(permissionList.size() > 0){
            Map<String, Object> params = new HashMap();
            params.clear();
            params.put("licenseGranted", true);
            List<Res> licenseGrantedList = resService.selectObjByMap(params);
            licenseGrantedList.forEach(licenseGranted -> {
                licenseGranted.setLicenseGranted(false);
                resService.update(licenseGranted);
            });
            params.clear();
            params.put("permissionCodeList", permissionList);
            List<Res> resList = resService.selectObjByMap(params);
            if(resList.size() > 0){
                resList.forEach(res -> {
                    res.setLicenseGranted(true);
                    resService.update(res);
                });
            }
        }
    }

    private Object updateExistingLicense(License existingLicense, String code, String uuid) {
        if (!code.equals(existingLicense.getLicense())) {

            existingLicense.setLicense(code);
            existingLicense.setFrom(0);
            existingLicense.setSystemSN(uuid);
            existingLicense.setStatus(0);
            if (!this.licenseTools.verifyExpiration(code)) {
                existingLicense.setStatus(2);
            }
            int flag = this.licenseService.update(existingLicense);
            if(flag > 0){
                String licenseInfo = AesEncryptUtils.decrypt(code);
                LicenseVo licenseVo = JSONObject.parseObject(licenseInfo, LicenseVo.class);
                updatePermission(licenseVo.getPermissionCodeList());
            }
            return ResponseUtil.ok("授权成功");
        }
        return ResponseUtil.badArgument("重复授权");
    }

    private Object createNewLicense(String code, String uuid) {
        License newLicense = new License();
        newLicense.setLicense(code);
        newLicense.setFrom(0);
        newLicense.setSystemSN(uuid);
        newLicense.setStatus(1);
        if (!this.licenseTools.verifyExpiration(code)) {
            newLicense.setStatus(2);
        }
        int flag = this.licenseService.save(newLicense);
        if(flag > 0){
            String licenseInfo = AesEncryptUtils.decrypt(code);
            LicenseVo licenseVo = JSONObject.parseObject(licenseInfo, LicenseVo.class);
            updatePermission(licenseVo.getPermissionCodeList());
        }
        return ResponseUtil.ok("授权成功");
    }

    @RequestMapping("/license")
    public void license() {
        // 1. 获取设备唯一申请码
        String uuid = SystemInfoUtils.getSerialNumber();

        // 2. 查询现有许可证
        List<License> licenseList = this.licenseService.query();
        License license = licenseList.isEmpty() ? null : licenseList.get(0);

        if (license != null) {
            updateLicenseStatus(license, uuid);
        }
    }

    private void updateLicenseStatus(License license, String uuid) {
        String systemSN = license.getSystemSN();
        boolean isAuthorized = true; // 是否检测授权码

        // 检查当前设备是否为初始化设备
        if (systemSN == null || systemSN.isEmpty()) {
            license.setSystemSN(uuid);
        } else if (!systemSN.equals(uuid)) {
            license.setFrom(1);
            license.setStatus(1);
            isAuthorized = false;
        } else {
            license.setFrom(0);
        }

        // 检测授权码是否已过期
        if (isAuthorized && license.getLicense() != null && !license.getLicense().isEmpty()) {
            checkLicenseExpiration(license);
        }

        // 更新许可证
        licenseService.update(license);
    }

    private void checkLicenseExpiration(License license) {
        String licenseInfo = license.getLicense();
        Map<String, Object> licenseData = null;

        try {
            licenseData = JSONObject.parseObject(AesEncryptUtils.decrypt(licenseInfo), Map.class);
        } catch (Exception e) {
            return; // 如果解密失败，直接返回
        }

        String endTimeStamp = (String) licenseData.get("expireTime");
        if (endTimeStamp != null && !endTimeStamp.isEmpty()) {
            long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳（单位秒）
            if (Long.parseLong(endTimeStamp) <= currentTime) {
                license.setStatus(2); // 许可证过期
            } else {
                license.setStatus(0); // 许可证有效
            }
        } else {
            license.setStatus(1); // 未授权
        }
    }

}
