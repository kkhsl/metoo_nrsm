package com.metoo.nrsm.core.manager.utils;

import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.LicenseVo;
import org.springframework.stereotype.Component;

@Component
public class LicenseUtils {

    /**
     * 计算授权日期
     * @param license
     */
    public static void calculateLicenseDays(LicenseVo license) {
        long currentTime = DateTools.currentTimeMillis();

        // 1. 计算总授权天数（不足一天算一天）
        long totalMillis = license.getEndTime() - license.getStartTime();
        int licenseDay = (int) Math.ceil((double) totalMillis / DateTools.ONEDAY_TIME);
        licenseDay = Math.max(licenseDay, 1); // 至少1天
        license.setLicenseDay(licenseDay);

        // 2. 计算已使用天数（不足一天也算一天）
        long usedMillis = currentTime - license.getStartTime();
        int useDay = (int) Math.ceil((double) usedMillis / DateTools.ONEDAY_TIME);
        useDay = Math.max(useDay, 1); // 至少1天
        license.setUseDay(useDay);

        // 3. 计算剩余天数（如果<=0，则返回0）
        long remainingMillis = license.getEndTime() - currentTime;
        int surplusDay = (int) Math.floor((double) remainingMillis / DateTools.ONEDAY_TIME);
        surplusDay = Math.max(surplusDay, 0); // 剩余天数最小为0（不能为负数）
        license.setSurplusDay(surplusDay);
    }

}
