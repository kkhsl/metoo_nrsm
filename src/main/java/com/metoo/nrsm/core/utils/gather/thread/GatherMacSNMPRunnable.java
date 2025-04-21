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
public class GatherMacSNMPRunnable implements Runnable {

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
        String ip = networkElement.getIp();
        try {
            long start = System.currentTimeMillis();
            log.debug("开始采集: {}", ip);

            macManager.getMac(networkElement, date);

            long cost = System.currentTimeMillis() - start;
            log.debug("采集完成: {}, 耗时: {}ms", ip, cost);
        } catch (Exception e) {
            log.error("采集设备 {} 异常: {}", ip, e.getMessage());
        } finally {
            latch.countDown();
            log.trace("计数器减1, 剩余: {}", latch.getCount());
        }
    }
}
