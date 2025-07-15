package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.LicenseDto;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.mapper.NetworkElementMapper;
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
import com.metoo.nrsm.entity.SystemUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

import static com.metoo.nrsm.core.utils.license.AesEncryptUtils.encrypt;

@RequestMapping("/license")
@RestController
@Slf4j
public class LicenseManagerController {

    @Autowired
    private ILicenseService licenseService;
    @Autowired
    private AesEncryptUtils aesEncryptUtils;
    @Autowired
    private LicenseTools licenseTools;
    @Autowired
    private IDiskService diskService;
    @Autowired
    private ISystemUsageService systemUsageService;
    @Autowired
    private IUnboundService unboundService;
    @Autowired
    private IPingIpConfigService pingIpConfigService;
    @Resource
    private NetworkElementMapper networkElementMapper;



    @RequestMapping("/all")
    @Scheduled(cron = "0 0 * * * ?")
    public Result hourlyReport() throws Exception {
        Map<String, Object> reportData = collectSystemInfo();
        //sendToPlatform(reportData);
        return ResponseUtil.ok(reportData);
    }

    public Map<String, Object> collectSystemInfo() throws Exception {
        Map<String, Object> reportData = new LinkedHashMap<>();

        // 1. 收集基础信息
        Map<String, Object> baseInfo = new LinkedHashMap<>();
        License obj = licenseService.query().get(0);
        String sn = SystemInfoUtils.getSerialNumber();
        String licenseInfo = aesEncryptUtils.decrypt(obj.getLicense());
        LicenseVo license = JSONObject.parseObject(licenseInfo, LicenseVo.class);
        calculateLicenseDays(license);
        Map<String, Object> licensesInfo = new LinkedHashMap<>();
        licensesInfo.put("Probe",license.isLicenseProbe());


        baseInfo.put("sn", sn);
        baseInfo.put("unit", license.getCustomerInfo());
        baseInfo.put("version", license.getLicenseVersion());
        baseInfo.put("licenseModule", licensesInfo);

        reportData.put("baseInfo", baseInfo);

        // 2. 收集资源利用率
        Map<String, Object> resourceUsage = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -31);
        Date startTime = calendar.getTime();
        calendar.add(Calendar.SECOND, +62);
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


    private boolean checkProcessStatus(String processName) {
        String status = checkProcessStatus.checkProcessStatus(processName);
        return (status != null && status.equalsIgnoreCase("true"));
    }

    // 发送数据到平台接口
    private void sendToPlatform(Map<String, Object> reportData) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://platform-api.example.com/monitor/data";

        try {
            // 添加时间戳和主机标识
            reportData.put("reportTime", new Date());
            reportData.put("hostId", System.getenv("HOST_ID")); // 或从配置读取

            // 转换并发送JSON数据
            String jsonData = JSON.toJSONString(reportData);
            restTemplate.postForObject(apiUrl, jsonData, String.class);

        } catch (Exception e) {
            log.error("平台接口上报失败: {}", e.getMessage());
        }
    }





    @RequestMapping("/systemInfo")
    public Object systemInfo() {
        try {
//            String sn = this.aesEncryptUtils.encrypt(this.systemInfoUtils.getBiosUuid());
//            String sn = SystemInfoUtils.getBiosUuid();
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
            String licenseInfo = this.aesEncryptUtils.decrypt(obj.getLicense());
            LicenseVo license = JSONObject.parseObject(licenseInfo, LicenseVo.class);

            // 计算使用天数和剩余天数计算授权天数
            calculateLicenseDays(license);

            System.out.println(JSONObject.toJSONString(license));
            return ResponseUtil.ok(license);
        } catch (Exception e) {
            // 使用日志记录异常信息
            return ResponseUtil.error("查询失败: " + e.getMessage());
        }
    }

    private void calculateLicenseDays(LicenseVo license) {
        long currentTime = DateTools.currentTimeMillis();
        license.setUseDay(DateTools.compare(currentTime, license.getStartTime()));

        long remainingMillis = license.getEndTime() - currentTime;
        int surplusDay = (int) Math.ceil((double) remainingMillis / DateTools.ONEDAY_TIME);
        surplusDay = Math.max(surplusDay, 1); // 确保最小值为1
        license.setSurplusDay(surplusDay);

//        license.setSurplusDay(DateTools.compare(license.getEndTime(), currentTime));

        license.setLicenseDay(DateTools.compare(license.getEndTime(), license.getStartTime()));
    }

    /**
     * 授权
     *
     * @param license
     * @return
     */
    @PutMapping("update")
    public Object license(@RequestBody Map<String, Object> license) throws Exception {

        String uuid = SystemInfoUtils.getSerialNumber();
        String code = license.get("license").toString();

        // 验证许可证合法性
        System.out.println("***license: " + code);
        boolean isValid = this.licenseTools.verifySN(uuid, code);

        String decrypt = this.aesEncryptUtils.decrypt(code);
        License parsed = JSONObject.parseObject(decrypt, License.class);
        int licenseUe = parsed.getLicenseDevice();
        int num = networkElementMapper.selectObjAll(null).size();
//        if (licenseUe<num){
//            return ResponseUtil.error("授权网元数不够");
//        }
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

    @PutMapping("sq")
    public Object sq(@RequestBody License licenseDto) throws Exception {
        License dto = new License();
        dto.setStartTime(licenseDto.getStartTime());
        dto.setEndTime(licenseDto.getEndTime());
        dto.setSystemSN(licenseDto.getSystemSN());
        dto.setType(licenseDto.getType());
        dto.setLicenseAC(true);
        dto.setLicenseProbe(true);
        dto.setLicenseVersion(licenseDto.getLicenseVersion());
        dto.setLicenseFireWall(licenseDto.getLicenseFireWall());
        dto.setLicenseRouter(licenseDto.getLicenseRouter());
        dto.setLicenseHost(licenseDto.getLicenseHost());
        dto.setLicenseUe(licenseDto.getLicenseUe());
        dto.setLicenseDevice(licenseDto.getLicenseDevice());
        dto.setCustomerInfo(licenseDto.getCustomerInfo());
        dto.setInsertTime(licenseDto.getInsertTime());
        String content = JSONObject.toJSONString(dto);
        System.out.println("加密前：" + content);

        String encrypt = encrypt(content, Global.AES_KEY);
        System.out.println("加密后：" + encrypt);
        return ResponseUtil.ok(encrypt);
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
            this.licenseService.update(existingLicense);
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
        this.licenseService.save(newLicense);
        return ResponseUtil.ok("授权成功");
    }

    /**
     * 授权
     *
     * @param //license
     * @return
     */
   /* @PutMapping("update")
    public Object license(@RequestBody Map license){
        String uuid = this.systemInfoUtils.getBiosUuid();
        // 验证license合法性
        String code = license.get("license").toString();
        boolean flag = this.licenseTools.verify(uuid, code);
        if(flag){
            License obj = this.licenseService.query().get(0);
            if(!obj.getLicense().equals(license)){
                obj.setLicense(code);
                obj.setFrom(0);
                obj.setSystemSN(uuid);
                obj.setStatus(0);
                // 格式化时间
                String startTime = obj.getStartTime();

                if(!this.verify(code)){
                    obj.setStatus(2);
                }
                this.licenseService.update(obj);
                return ResponseUtil.ok("授权成功");
            }
            return ResponseUtil.badArgument("重复授权");
        }
        return ResponseUtil.error("非法授权码");
    }*/
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
            licenseData = JSONObject.parseObject(aesEncryptUtils.decrypt(licenseInfo), Map.class);
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


    @PostMapping("/generate")
    public Object license(@RequestBody(required = false) LicenseDto dto) {
        if (dto != null && dto.getSystemSN() != null) {
            try {
                // 解密系统序列号（如需要）
                // String sn = this.aesEncryptUtils.decrypt(dto.getSystemSN());
                // dto.setSystemSN(sn);

                // 处理过期时间（如需要）
                // long currentTime = processExpireTime(dto.getExpireTime());

                // 返回加密的许可证信息
                String encryptedLicense = this.aesEncryptUtils.encrypt(JSONObject.toJSONString(dto));
                return ResponseUtil.ok(encryptedLicense);
            } catch (Exception e) {
                return ResponseUtil.error("申请码错误");
            }
        }
        return ResponseUtil.error("请求体中缺少有效的系统序列号");
    }

}
