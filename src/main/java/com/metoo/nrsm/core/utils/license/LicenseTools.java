package com.metoo.nrsm.core.utils.license;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.entity.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LicenseTools {

    /**
     * 验证License合法性
     *
     * @return
     */
    public boolean verifySN(String systemSN, String code) {
        try {
            String decrypt = AesEncryptUtils.decrypt(code);
            System.out.println("&&&decrypt" + decrypt);

            License license = JSONObject.parseObject(decrypt, License.class);
            System.out.println("&&&decrypt" + JSONObject.toJSONString(license));

            String sn = license.getSystemSN();

            if (sn.equals(systemSN)) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyExpiration(String code) {
        // 检测授权码是否已过期
        if (code != null) {
            License license = null;
            try {
                license = JSONObject.parseObject(AesEncryptUtils.decrypt(code), License.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (license != null) {
                Long endTime = license.getEndTime();// 有效期
                if (endTime != null) {
                    long currentTime = DateTools.currentTimeMillis();
                    if (endTime - currentTime >= 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
