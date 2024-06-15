package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.mapper.LicenseMapper;
import com.metoo.nrsm.core.service.ILicenseService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.entity.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LicenseServiceImpl implements ILicenseService {

    @Autowired
    private LicenseMapper licenseMapper;
    @Autowired
    private AesEncryptUtils aesEncryptUtils;

    @Override
    public License detection() {

//        String uuid = SystemInfoUtils.getBiosUuid();

        String uuid = SystemInfoUtils.getSerialNumber();

        // 2，查询并比对当前设备是否允许授权
        List<License> licenseList = this.licenseMapper.query();
        License license = null;
        if (licenseList.size() > 0) {
            license = licenseList.get(0);
        }
        if (license == null) {
//            String uuid = SystemInfoUtils.getBiosUuid();
//            License instance = new License();
//            instance.setFrom(0);
//            instance.setSystemSN(uuid);
//            instance.setStatus(1);
//            this.save(instance);
//            return instance;
            return null;
        }
        // 检测授权码是否已过期
        if (license.getLicense() != null && !license.getLicense().isEmpty()) {
                String systemSN = license.getSystemSN();
                boolean from = true;// 是否检测授权码：不在同意设备时不允许使用
                // 初始化设备，并检查当前设备是否为初始化设备
                if (systemSN == null || systemSN.isEmpty()) {// 申请码为空
                    license.setSystemSN(uuid);
                } else {
                    if (!systemSN.equals(uuid)) {// 申请码不一致
                        license.setFrom(1);
                        license.setStatus(1);
                    } else {// 一致时恢复来源
                        license.setFrom(0);
                    }
                }
                // 检测授权码是否已过期
                if (from) {
                    String licenseInfo = license.getLicense();
                    Map map = null;
                    try {
                        map = JSONObject.parseObject(this.aesEncryptUtils.decrypt(licenseInfo), Map.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (map != null) {
                        Long endTime = Long.parseLong(map.get("endTime").toString());// 有效期
                        if (endTime != null) {
                            long currentTime = DateTools.currentTimeMillis();
                            if (endTime - currentTime <= 0) {
                                license.setStatus(2);// 过期
                            } else {
                                license.setStatus(0);// 恢复为未过期
                            }
                        }
                    } else {
                        license.setStatus(1);// 未授权
                    }
                }
                // 更新License 优化是否执行更新
                this.update(license);
                return license;
            }
        return null;
    }

    @Override
    public List<License> query() {
        return this.licenseMapper.query();
    }

    @Override
    public int save(License instance) {
        return this.licenseMapper.save(instance);
    }

    @Override
    public int update(License instance) {
        return this.licenseMapper.update(instance);
    }

}
