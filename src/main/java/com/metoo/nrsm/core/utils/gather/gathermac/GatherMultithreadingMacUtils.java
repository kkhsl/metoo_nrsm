package com.metoo.nrsm.core.utils.gather.gathermac;

import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalCountService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.utils.gather.concurrent.GatherDataThreadPool;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.TerminalCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-23 10:39
 */
@Slf4j
@Component
public class GatherMultithreadingMacUtils {

    @Autowired
    private IMacService macService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private ITerminalCountService terminalCountService;

    private final GatherDataThreadPool threadPool;

    @Autowired
    public GatherMultithreadingMacUtils(GatherDataThreadPool threadPool) {
        this.threadPool = threadPool;
    }
    public void gatherMacThread(List<NetworkElement> networkElements, Date date) {

        log.info("Mac start =========");

        if(networkElements.size() > 0){

            this.terminalService.syncTerminal(date);

            // 统计终端ip数量
            try {
                Map terminal = this.terminalService.terminalCount();
                if(terminal != null){
                    TerminalCount count = new TerminalCount();
                    count.setAddTime(date);
                    count.setV4ip_count(Integer.parseInt(String.valueOf(terminal.get("v4ip_count"))));
                    count.setV6ip_count(Integer.parseInt(String.valueOf(terminal.get("v6ip_count"))));
                    count.setV4ip_v6ip_count(Integer.parseInt(String.valueOf(terminal.get("v4ip_v6ip_count"))));
                    terminalCountService.save(count);
                }else{
                    TerminalCount count = new TerminalCount();
                    count.setAddTime(date);
                    count.setV4ip_count(0);
                    count.setV6ip_count(0);
                    count.setV4ip_v6ip_count(0);
                    terminalCountService.save(count);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            // 同步终端到终端历史表
            this.terminalService.syncTerminalToTerminalHistory();

            // mac 复制数据写入标签和ip地址信息等
            this.gatherMacUtils.copyGatherData(date);

            this.macService.truncateTableGather();

            CountDownLatch latch = new CountDownLatch(networkElements.size() * 4);

            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion()) || StringUtils.isBlank(networkElement.getCommunity())){
                    latch.countDown();
                    continue;
                }
//                threadPool.execute((new GatherMacHostNameRunnable(networkElement, date, latch));
            }

            try {

                latch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Mac end =========");
    }

    Lock lock = new ReentrantLock();

    public synchronized void gatherMacThread2(Date date) {
//        lock.lock();
        try {

            List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
            if(networkElements.size() > 0){

                this.terminalService.syncTerminal(date);

                // 同步终端到终端历史表
                this.terminalService.syncTerminalToTerminalHistory();

                // mac 复制数据写入标签和ip地址信息等
                this.gatherMacUtils.copyGatherData(date);

                this.macService.truncateTableGather();

                for (NetworkElement networkElement : networkElements) {
//                    threadPool.execute(new GatherMacHostNameRunnable(networkElement, date));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

}
