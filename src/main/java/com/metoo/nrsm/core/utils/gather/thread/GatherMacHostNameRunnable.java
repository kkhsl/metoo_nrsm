package com.metoo.nrsm.core.utils.gather.thread;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherMacHostNameRunnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherMacHostNameRunnable() {
    }

    public GatherMacHostNameRunnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherMacHostNameRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + ": gethostname.py + : " + networkElement.getIp());
        try {
            String path = Global.PYPATH + "gethostname.py";

            PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

            String[] params = {networkElement.getIp(), networkElement.getVersion(),
                    networkElement.getCommunity()};

            String hostname = pythonExecUtils.exec2(path, params);
            if(StringUtils.isNotEmpty(hostname)){
                GatherDataThreadPool.getInstance().addThread(new GatherMacGetlldpRunnable(networkElement, date, hostname, latch));

                GatherDataThreadPool.getInstance().addThread(new GatherMacGetMacRunnable(networkElement, date, hostname, latch));

                GatherDataThreadPool.getInstance().addThread(new GatherMacGetPortMacRunnable(networkElement, date, hostname, latch));
            }else{
                if(latch != null){
                    latch.countDown();
                    latch.countDown();
                    latch.countDown();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(latch != null){
                latch.countDown();
                latch.countDown();
                latch.countDown();
            }
        } finally {
            if(latch != null){
                latch.countDown();
            }

        }
    }
}
