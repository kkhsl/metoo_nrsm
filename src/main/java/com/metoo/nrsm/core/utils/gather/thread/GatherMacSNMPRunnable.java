package com.metoo.nrsm.core.utils.gather.thread;

import com.metoo.nrsm.core.utils.gather.snmp.utils.MacManager;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherMacSNMPRunnable implements Runnable{

    private NetworkElement networkElement;

    private MacManager macManager;

    private Date date;

    private CountDownLatch latch;

    public GatherMacSNMPRunnable() {
    }

    public GatherMacSNMPRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }

    public GatherMacSNMPRunnable(NetworkElement networkElement, MacManager macManager, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.macManager = macManager;
        this.date = date;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            macManager.getMac(networkElement, date);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }

}
