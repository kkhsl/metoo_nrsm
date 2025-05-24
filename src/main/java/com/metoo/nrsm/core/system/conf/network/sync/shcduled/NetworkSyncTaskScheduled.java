package com.metoo.nrsm.core.system.conf.network.sync.shcduled;

import com.metoo.nrsm.core.system.conf.network.sync.LocalNetplanSyncService;
import com.metoo.nrsm.core.system.conf.network.sync.service.NetplanSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NetworkSyncTaskScheduled {

    private final NetplanSyncService netplanSyncService;

    public NetworkSyncTaskScheduled(NetplanSyncService netplanSyncService) {
        this.netplanSyncService = netplanSyncService;
    }


    /**
     * 每30分钟同步一次
     * 定时表达式说明：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void scheduledSync() {
        try {
            System.out.println("定时任务开始同步网络接口配置...");
            netplanSyncService.syncInterfaces();
            System.out.println("定时网络接口配置同步完成");
        } catch (Exception e) {
            System.err.println("定时同步网络接口配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 或者使用固定间隔方式（每30分钟）
     */
//    @Scheduled(fixedRate = 30 * 60 * 1000)
//    public void scheduledSyncAlternative() {
//        // 同上实现
//    }
}
