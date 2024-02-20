package com.metoo.nrsm.core.utils.gather;

import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Calendar;
import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-18 16:03
 */
@Configuration
public class Scheduled {

    @Value("${task.switch.is-open}")
    private boolean flag;
    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;
    @Autowired
    private IArpService arpService;
    @Autowired
    private IMacService macService;

    @Autowired
    private IGatherService gatherService;

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherDhcp() {
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("DHCP Start......");
            try {
                dhcpService.gather(DateTools.gatherDate());

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("DHCP End......" + (System.currentTimeMillis()-time));
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherDhcp6() {
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("DHCP6 Start......");
            try {
                dhcp6Service.gather(DateTools.gatherDate());

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("DHCP6 End......" + (System.currentTimeMillis()-time));
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherArp() {
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("arp Start......");
            try {
//                arpService.gatherArp(date);
                gatherService.gatherArp(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("arp End......" + (System.currentTimeMillis()-time));
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherMac() {
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("mac Start......");
            try {
                macService.gatherMac(DateTools.gatherDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("mac End......" + (System.currentTimeMillis()-time));
        }
    }
}
