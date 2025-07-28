package com.metoo.nrsm.core.client.traffic.schedule;


import com.metoo.nrsm.core.manager.utils.SseManager;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Configuration
public class TrafficPushScheduler {

    @Autowired
    private ApiExecUtils apiExecUtils;

    @Value("${task.switch.traffic.is-open}")
    private boolean traffic;

    // 避免任务堆积，造成任务挂起，
    private final ReentrantLock lock = new ReentrantLock();

    SseManager sseManager=new SseManager();

    /**
     * 任务在 00:00 启动，执行耗时 6 分钟（到 00:06）00:05 时，新任务会启动（即使前一个任务未完成）
     * 该任务必须使用当前表达式
     */
//    @Scheduled(cron = "0 */1 * * * ?")
//    public void api() {
//        log.info("流量推送...");
//        if (traffic) {
//            if (lock.tryLock()) {
//                final String TASK_TYPE = "TRAFFIC"; // 任务类型标识
//                try {
//                    sseManager.sendLogToAll(TASK_TYPE, "流量推送任务开始");
//                    Long time = System.currentTimeMillis();
//                    log.info("流量推送开始：{}", time);
//                    apiExecUtils.exec();
//                    String execTime = "流量采集时间:" + DateTools.measureExecutionTime(System.currentTimeMillis() - time);
//                    log.info(execTime);
//                    sseManager.sendLogToAll(TASK_TYPE, execTime);
//                    sseManager.sendLogToAll(TASK_TYPE, "流量采集任务完成");
//                } catch (Exception e) {
//                    sseManager.sendLogToAll(TASK_TYPE, "流量采集任务失败");
//                    log.error("流量推送失败：{}", e.getMessage());
//                } finally {
//                    lock.unlock();
//                }
//            }
//        }
//    }

    private volatile boolean isRunningTraffic = false;

    @Scheduled(cron = "0 */5 * * * ?")
    public void arp() {
        if (traffic && !isRunningTraffic) {
            log.info("流量采集任务开始");
            isRunningTraffic = true;
            final String TASK_TYPE = "TRAFFIC"; // 任务类型标识
            try {
                sseManager.sendLogToAll(TASK_TYPE, "流量采集任务开始");
                Long time = System.currentTimeMillis();
                apiExecUtils.exec();
                String execTime = "流量采集时间:" + DateTools.measureExecutionTime(System.currentTimeMillis() - time);
                log.info(execTime);
                sseManager.sendLogToAll(TASK_TYPE, execTime);
                sseManager.sendLogToAll(TASK_TYPE, "流量采集任务完成");
            } catch (Exception e) {
                e.printStackTrace();
                sseManager.sendLogToAll(TASK_TYPE, "流量采集任务异常: " + e.getMessage());
                log.error("流量采集任务异常: {}", e.getMessage());
            } finally {
                isRunningTraffic = false;
            }
        }
    }
}
