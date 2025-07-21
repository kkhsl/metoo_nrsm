package com.metoo.nrsm.core.client.traffic.schedule;


import com.metoo.nrsm.core.utils.api.ApiExecUtils;
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

    /**
     * 任务在 00:00 启动，执行耗时 6 分钟（到 00:06）00:05 时，新任务会启动（即使前一个任务未完成）
     * 该任务必须使用当前表达式
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void api() {
        if (traffic) {
            if (lock.tryLock()) {
                try {
                    Long time = System.currentTimeMillis();
                    log.info("流量推送开始：{}", time);
                    apiExecUtils.exec();
                    log.info("流量推送完成：{}", (System.currentTimeMillis() - time));
                } catch (Exception e) {
                    log.error("流量推送失败：{}", e.getMessage());
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
