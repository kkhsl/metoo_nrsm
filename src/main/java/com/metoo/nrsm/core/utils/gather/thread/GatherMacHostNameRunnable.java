//package com.metoo.nrsm.core.utils.gather.thread;
//
//import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
//import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
//import com.metoo.nrsm.entity.NetworkElement;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//
//import java.utils.Date;
//import java.utils.concurrent.CountDownLatch;
//
///**
// * @author HKK
// * @version 1.0
// * @date 2024-02-20 11:36
// */
//@Slf4j
//@Component
//public class GatherMacHostNameRunnable implements Runnable{
//
//    private NetworkElement networkElement;
//
//    private Date date;
//
//    private CountDownLatch latch;
//
//    public GatherMacHostNameRunnable() {
//    }
//
//    public GatherMacHostNameRunnable(NetworkElement networkElement, Date date) {
//        this.networkElement = networkElement;
//        this.date = date;
//    }
//
//    public GatherMacHostNameRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
//        this.networkElement = networkElement;
//        this.date = date;
//        this.latch = latch;
//    }
//
//
//
//    @Override
//    public void run() {
//        try {
//            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
//            String result = SNMPv2Request.getDeviceName(snmpParams);
//
//            if(StringUtils.isNotEmpty(result)){
//
//                GatherDataThreadPool.getInstance().addThread(new GatherMacGetlldpRunnable(networkElement, date, result, latch));
//
//                GatherDataThreadPool.getInstance().addThread(new GatherMacGetMacRunnable(networkElement, date, result, latch));
//
//                GatherDataThreadPool.getInstance().addThread(new GatherMacGetPortMacRunnable(networkElement, date, result, latch));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if(latch != null){
//                latch.countDown();
//            }
//
//        }
//    }
//}
