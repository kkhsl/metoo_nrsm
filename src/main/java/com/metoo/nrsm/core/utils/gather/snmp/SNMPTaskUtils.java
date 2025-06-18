package com.metoo.nrsm.core.utils.gather.snmp;

import com.metoo.nrsm.core.service.IGatherService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.snmp.utils.DeviceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;

@Slf4j
@Configuration
public class SNMPTaskUtils {

//    @Value("${task.switch.is-open}")
//    private boolean flag;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private IGatherService gatherService;

    private static final boolean FLAG = true;

    // 获取主机名,判断主机是否可用,并记录状态到redis
    public void getDeviceNameByIpAndCommunityVersion() {
        if(FLAG){
            Long time = System.currentTimeMillis();
            log.info("Snmp status start......");
            try {
                deviceManager.saveAvailableDevicesToRedis();
            } catch (Exception e) {
                log.error("", e);
            }
            log.info("Snmp status end......" + (System.currentTimeMillis() - time));
        }
    }



    public void ipv4() {
        Long time=System.currentTimeMillis();
        try {
            gatherService.gatherIpv4Thread(DateTools.gatherDate(), new ArrayList<>());
        } catch (Exception e) {
        }
        log.info("Ipv4 End......" + (System.currentTimeMillis()-time));
    }

    public void port() {
        Long time = System.currentTimeMillis();
        try {
            gatherService.gatherPort(DateTools.gatherDate(), new ArrayList<>());
        } catch (Exception e) {
        }
        log.info("Port End......" + (System.currentTimeMillis()-time));
    }

}
