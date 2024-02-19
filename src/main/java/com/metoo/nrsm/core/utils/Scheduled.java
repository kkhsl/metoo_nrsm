package com.metoo.nrsm.core.utils;

import com.metoo.nrsm.core.service.*;
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

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherDhcp() {
        //下面正常使用业务代码即可
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("DHCP Start......");
            // 采集时间
            Calendar cal = Calendar.getInstance();
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            Date date = cal.getTime();

            try {
                dhcpService.gather(date);

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("DHCP End......" + (System.currentTimeMillis()-time));
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherDhcp6() {
        //下面正常使用业务代码即可
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("DHCP6 Start......");
            // 采集时间
            Calendar cal = Calendar.getInstance();
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            Date date = cal.getTime();

            try {
                dhcp6Service.gather(date);

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("DHCP6 End......" + (System.currentTimeMillis()-time));
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherArp() {
        //下面正常使用业务代码即可
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("arp Start......");
            // 采集时间
            Calendar cal = Calendar.getInstance();
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            Date date = cal.getTime();
            try {
                arpService.gatherArp(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("arp End......" + (System.currentTimeMillis()-time));
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void gatherMac() {
        //下面正常使用业务代码即可
        if(flag){
            Long time=System.currentTimeMillis();
            System.out.println("mac Start......");
            // 采集时间
            Calendar cal = Calendar.getInstance();
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            Date date = cal.getTime();
            try {
                macService.gatherMac(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("mac End......" + (System.currentTimeMillis()-time));
        }
    }
}
