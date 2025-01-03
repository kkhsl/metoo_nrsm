package com.metoo.nrsm.core.utils.gather.gathermac;

import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalCountService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.utils.gather.thread.*;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Terminal;
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


    /**
     * 并发采集，当采集时长超过定时任务时长时，导致第二次采集开始后，第一次未结束线程数据写入到第二次采集任务中
     *
     * 解决方法：
     *  方法一：
     *      使用redis互斥锁，当第二次任务开始时，查询锁是否被占用，被占用则跳过此次采集
     *  方法二：
     *      使用数据库表锁（尝试使用这种方式）
 *      方法三：
     *      使用线程锁
 *      方法四：
     *       Scheduled：java定时任务会等待第一次任务执行完毕，才开始第二次采集
     *       使用“CountDownLatch”同步功率类，解决并发采集，导致的多次采集问题
     *
     * @param date
     */
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
                GatherDataThreadPool.getInstance().addThread(new GatherMacHostNameRunnable(networkElement, date, latch));
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
                    GatherDataThreadPool.getInstance().addThread(new GatherMacHostNameRunnable(networkElement, date));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            lock.unlock(); // 释放锁
        }
    }
//    public void gatherMacThread(Date date) {
//        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
//        if(networkElements.size() > 0){
//
//
//            // mac 复制数据写入标签和ip地址信息等
//            this.gatherMacUtils.copyGatherData(date);
//
//            // 增加写锁（查询是否锁表，锁表跳过本次采集，避免等待（死锁））
//            // 增加写锁后，不能操作其他表，所以把锁放到当前位置
////            this.macService.lock();
//
//            this.macService.truncateTableGather();
//
//            CountDownLatch latch = new CountDownLatch(networkElements.size() * 4);
//
//            for (NetworkElement networkElement : networkElements) {
//
//
////                String path = Global.PYPATH + "gethostname.py";
////                String[] params = {networkElement.getIp(), networkElement.getVersion(),
////                        networkElement.getCommunity()};
////                String hostname = PythonExecUtils.exec(path, params);
//
////                Thread getlldp = new Thread(new GatherMacGetlldpRunnable(networkElement, date, hostname));
////                getlldp.start();
////
////                Thread getmac = new Thread(new GatherMacGetMacRunnable(networkElement, date, hostname));
////                getmac.start();
////
////                Thread getportmac = new Thread(new GatherMacGetPortMacRunnable(networkElement, date, hostname));
////                getportmac.start();
//
//
////                String path = Global.PYPATH + "gethostname.py";
////                String[] params = {networkElement.getIp(), networkElement.getVersion(),
////                        networkElement.getCommunity()};
////                String hostname = PythonExecUtils.exec(path, params);
////                ThreadPool.getInstance().addThread(new GatherMacGetlldpRunnable(networkElement, date, hostname));
////
////                ThreadPool.getInstance().addThread(new GatherMacGetMacRunnable(networkElement, date, hostname));
////
////                ThreadPool.getInstance().addThread(new GatherMacGetPortMacRunnable(networkElement, date, hostname));
//
//
//                ThreadPool.getInstance().addThread(new GatherMacHostNameRunnable(networkElement, date, latch));
//            }
//
//            try {
//                log.info("latch count: "+ latch.getCount());
//
//                latch.await();// 等待结果线程池线程执行结束
//
//                log.info("run end......" + latch.getCount());
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
