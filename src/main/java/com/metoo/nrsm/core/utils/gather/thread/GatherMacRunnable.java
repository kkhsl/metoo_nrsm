package com.metoo.nrsm.core.utils.gather.thread;

import com.metoo.nrsm.entity.NetworkElement;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-12 15:17
 */
public class GatherMacRunnable implements Runnable {

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherMacRunnable() {
    }

    public GatherMacRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            GatherDataThreadPool.getInstance().addThread(new GatherMacHostNameRunnable(networkElement, date, latch));

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
