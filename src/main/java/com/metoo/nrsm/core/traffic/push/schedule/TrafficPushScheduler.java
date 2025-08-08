package com.metoo.nrsm.core.traffic.push.schedule;


import com.metoo.nrsm.core.manager.utils.SseManager;
import com.metoo.nrsm.core.traffic.push.utils.TrafficPushExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class TrafficPushScheduler {

    @Autowired
    private TrafficPushExecUtils trafficPushExecUtils;

    @Value("${task.switch.traffic.is-open}")
    private boolean traffic;

    SseManager sseManager=new SseManager();

    private volatile boolean isRunningTraffic = false;
    @Scheduled(cron = "0 */5 * * * ?")
    public void traffic() {
        if (traffic && !isRunningTraffic) {
            log.info("流量采集任务开始");
            isRunningTraffic = true;
            final String TASK_TYPE = "TRAFFIC"; // 任务类型标识
            try {
                sseManager.sendLogToAll(TASK_TYPE, "流量采集任务开始");
                Long time = System.currentTimeMillis();
                trafficPushExecUtils.pushTraffic();
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
