package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.LicenseDto;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.mapper.NetworkElementMapper;
import com.metoo.nrsm.core.service.ILicenseService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.utils.license.LicenseTools;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.entity.License;
import com.metoo.nrsm.entity.NetworkElement;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.metoo.nrsm.core.utils.license.AesEncryptUtils.encrypt;

@RequestMapping("/license")
@RestController
public class LicenseManagerController {

    @Autowired
    private ILicenseService licenseService;
    @Autowired
    private AesEncryptUtils aesEncryptUtils;
    @Autowired
    private LicenseTools licenseTools;

    @Resource
    private NetworkElementMapper networkElementMapper;

    public static void main(String[] args) {
//        long currentTime = System.currentTimeMillis();
//        System.out.println(currentTime);
//        long ONEDAY_TIME = 24*60*60;
//        System.out.println(ONEDAY_TIME);
//        int day = (1653232116 - 1653019716)/(24*60*60);
//        System.out.println(day);

        // 当前日期时间戳
        String currentDate = DateTools.dateToStr(new Date(), DateTools.FORMAT_yyyyMMdd);
        long currentTime = DateTools.strToLong(currentDate + DateTools.TIME_000000, DateTools.FORMAT_yyyyMMddHHmmss);
        long startTime = DateTools.strToLong("20220519000000", DateTools.FORMAT_yyyyMMddHHmmss);

        int useDay = (int) ((currentTime - startTime) / DateTools.ONEDAY_TIME);

        System.out.println(useDay);
        long endTime = DateTools.strToLong("20220521000000", DateTools.FORMAT_yyyyMMddHHmmss);
        int surplusDay = (int) ((endTime - currentTime) / DateTools.ONEDAY_TIME);
        System.out.println(surplusDay);
    }

    @Test
    public void test(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = sdf.parse("20220522");
            System.out.println(date);
            System.out.println(date.getTime());
            Calendar cd = Calendar.getInstance();
            cd.setTime(date);
            System.out.println(cd.getTime());
            System.out.println(sdf.format(cd.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testTimeStamp(){
        long curremtTime = 1652976000000L;
        long startTime = 1652889600000L;
        long endTime = 1653062400000L;
        int useDay = (int) ((int)(curremtTime - startTime) / DateTools.ONEDAY_TIME);
        System.out.println(useDay);
        int surplusDay = (int) ((int)(endTime - curremtTime) / DateTools.ONEDAY_TIME);
        System.out.println(surplusDay);
        System.out.println(DateTools.longToDate(endTime,"yyyyMMdd"));
    }

    @RequestMapping("/systemInfo")
    public Object systemInfo(){
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

//   ,produces = "application/json; charset=utf-8"
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

            // 计算使用天数和剩余天数
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
        license.setSurplusDay(DateTools.compare(license.getEndTime(), currentTime));
    }

    /**
     * 授权
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
        if (licenseUe<num){
            return ResponseUtil.error("授权网元数不够");
        }
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
