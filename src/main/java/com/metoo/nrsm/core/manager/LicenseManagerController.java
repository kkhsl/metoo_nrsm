package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.LicenseDto;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.service.ILicenseService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.utils.license.LicenseTools;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.entity.License;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/license")
@RestController
public class LicenseManagerController {

    @Autowired
    private ILicenseService licenseService;
    @Autowired
    private AesEncryptUtils aesEncryptUtils;
    @Autowired
    private LicenseTools licenseTools;

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
    public Object query(){
        License obj = this.licenseService.query().get(0);

//        String sn = SystemInfoUtils.getBiosUuid();

        String uuid = SystemInfoUtils.getSerialNumber();

        if(!uuid.equals(obj.getSystemSN())){
            return ResponseUtil.error(413,"未授权设备");
        }
        try {
            String licenseInfo = this.aesEncryptUtils.decrypt(obj.getLicense());
            System.out.println("====licenseInfo:" + licenseInfo);
            LicenseVo license = JSONObject.parseObject(licenseInfo, LicenseVo.class);
//            if(license != null){
//                SysConfig sysConfig = this.sysConfigService.select();
//                String token = sysConfig.getNspmToken();
//                String url = "/topology/ums/getLicenseInfo.action";
//                TopoNodeDto dto = new TopoNodeDto();
//                dto.setStart(0);
//                dto.setLimit(20);
//                switch (license.getType()){
//                    case "0":
//                        license.setType("试用版");
//                        break;
//                    case "1":
//                        license.setType("授权版");
//                        break;
//                    case "2":
//                        license.setType("终身版");
//                        break;
//                    default:
//                        license.setType("破解版");
//                        break;
//                }
//                try {
//                    Object object = this.nodeUtil.getBody(dto, url, token);
//                    if(object != null){
//                        JSONObject result = JSONObject.parseObject(object.toString());
//                        String data = result.get("data").toString();
//                        JSONObject json = JSONObject.parseObject(data);
//                        long currentTime = DateTools.currentTimeMillis();
//                        int useDay = DateTools.compare(currentTime, license.getStartTime());
//                        license.setUseDay(useDay);
//                        int surplusDay = DateTools.compare(license.getEndTime(), currentTime);
//                        license.setSurplusDay(surplusDay);
//                        license.setLicenseDay(DateTools.compare(license.getEndTime(), license.getStartTime()));
//                        license.setUseFirewall(Integer.parseInt(json.get("currentFwNum").toString()));
//                        license.setUseRouter(Integer.parseInt(json.get("currentRouterNum").toString()));
//                        license.setUseHost(Integer.parseInt(json.get("currentHostNum").toString()));
//                        license.setUseUe(Integer.parseInt(json.get("currentGatewayNum").toString()));
//                    }
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                }catch (HttpClientErrorException e){
//                    e.printStackTrace();;
//                }
//            }
            long currentTime = DateTools.currentTimeMillis();

            int useDay = DateTools.compare(currentTime, license.getStartTime());
            license.setUseDay(useDay);

            int surplusDay = DateTools.compare(license.getEndTime(), currentTime);
            license.setSurplusDay(surplusDay);

            System.out.println(JSONObject.toJSONString(license));
            return ResponseUtil.ok(license);
        } catch (Exception e) {
             e.printStackTrace();
        }
        return ResponseUtil.ok();
    }

    /**
     * 授权
     * @param license
     * @return
     */
    @PutMapping("update")
    public Object license(@RequestBody Map license){
//        String uuid = SystemInfoUtils.getBiosUuid();
        String uuid = SystemInfoUtils.getSerialNumber();

        // 验证license合法性
        System.out.println("***license" + license.get("license"));

        String code = license.get("license").toString();

        boolean flag = this.licenseTools.verifySN(uuid, code);

        if(flag){
            List<License> list = this.licenseService.query();
            if(list.size() > 0){
                License obj = list.get(0);
                if(!code.equals(obj.getLicense())){
                    obj.setLicense(code);
                    obj.setFrom(0);
                    obj.setSystemSN(uuid);
                    obj.setStatus(0);
                    if(!this.licenseTools.verifyExpiration(code)){
                        obj.setStatus(2);
                    }
                    this.licenseService.update(obj);
                    return ResponseUtil.ok("授权成功");
                }
                return ResponseUtil.badArgument("重复授权");
            }else{
                License instance = new License();
                instance.setLicense(code);
                instance.setFrom(0);
                instance.setSystemSN(uuid);
                instance.setStatus(1);
                if(!this.licenseTools.verifyExpiration(code)){
                    instance.setStatus(2);
                }
                this.licenseService.save(instance);
                return ResponseUtil.ok("授权成功");
            }
        }
        return ResponseUtil.error("非法授权码");
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
    public void license(){
        // 1，获取设备唯一申请码
//        String uuid = SystemInfoUtils.getBiosUuid();

        String uuid = SystemInfoUtils.getSerialNumber();

        // 2，查询并比对当前设备是否允许授权
        List<License> licenseList = this.licenseService.query();
        License license = null;
        if(licenseList.size() > 0){
            license = licenseList.get(0);
        }
        if(license != null){
            String systemSN = license.getSystemSN();
            boolean from = true;// 是否检测授权码：不在同意设备时不允许使用
            // 初始化设备，并检查当前设备是否为初始化设备
            if(systemSN == null || systemSN.isEmpty()){// 申请码为空
                license.setSystemSN(uuid);
            }else{
                if(!systemSN.equals(uuid)){// 申请码不一致
                    license.setFrom(1);
                    license.setStatus(1);
                    from = false;
                }else{// 一致时恢复来源
                    license.setFrom(0);
                }
            }
            // 检测授权码是否已过期
            if(from && license.getLicense() != null && !license.getLicense().isEmpty()){
                String licenseInfo = license.getLicense();
                Map map = null;
                try {
                    map = JSONObject.parseObject(aesEncryptUtils.decrypt(licenseInfo), Map.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(map != null){
                    String endTimeStamp = map.get("expireTime").toString();// 有效期
                    if(endTimeStamp != null && !endTimeStamp.isEmpty()){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        long currentTime = calendar.getTimeInMillis();
                        long timeStampSec = currentTime / 1000;// 13位时间戳（单位毫秒）转换为10位字符串（单位秒）
                        String timestamp = String.format("%010d", timeStampSec);// 当前时间
                        if(Long.valueOf(endTimeStamp).compareTo(Long.valueOf(timestamp)) <= 0){
                            license.setStatus(2);// 过期
                        }else{
                            license.setStatus(0);// 恢复为未过期
                        }
                    }
                }
                license.setStatus(1);// 未授权
            }
            // 更新License
            licenseService.update(license);
        }
    }




    @PostMapping("/generate")
    public Object license(@RequestBody(required=false) LicenseDto dto){
        if(dto.getSystemSN() != null){
//            String sn = null;
            try {
//                sn = this.aesEncryptUtils.decrypt(dto.getSystemSN());
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(dto.getExpireTime());
//                long currentTime = calendar.getTimeInMillis();
//                long timeStampSec = currentTime / 1000;// 13位时间戳（单位毫秒）转换为10位字符串（单位秒）
//                String timestamp = String.format("%010d", timeStampSec);// 当前时间
                Map map = new HashMap();
//                dto.setSystemSN(sn);

                return this.aesEncryptUtils.encrypt(JSONObject.toJSONString(dto));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.error("申请码错误");
            }
        }
        return null;
    }

}
