package com.metoo.nrsm.core.utils.system;

import com.metoo.nrsm.core.service.IDiskService;
import com.metoo.nrsm.core.service.ISystemUsageService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.entity.Disk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class SystemUsageCollecting {

    @Autowired
    private ISystemUsageService systemUsageService;
    @Autowired
    private IDiskService diskService;


    @Scheduled(cron = "0 */1 * * * ?")
    public void dhcp6() {
        Long time=System.currentTimeMillis();
        log.info("System usage start ......");
        try {
            systemUsageService.saveSystemUsageToDatabase();
            diskService.getRootDiskSpaceInformation();

        } catch (Exception e) {
            log.error("Error occurred during System usage", e);
        }
        log.info("System usage end ......" + (System.currentTimeMillis()-time));
    }
}
