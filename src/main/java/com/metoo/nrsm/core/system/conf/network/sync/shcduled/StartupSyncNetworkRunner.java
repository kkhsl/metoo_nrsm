package com.metoo.nrsm.core.system.conf.network.sync.shcduled;

import com.metoo.nrsm.core.system.conf.network.sync.LocalNetplanSyncService;
import com.metoo.nrsm.core.system.conf.network.sync.service.NetplanSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupSyncNetworkRunner implements CommandLineRunner {

    private final NetplanSyncService netplanSyncService;  // 使用接口而非具体实现

    public StartupSyncNetworkRunner(NetplanSyncService netplanSyncService) {
        this.netplanSyncService = netplanSyncService;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("应用启动时开始同步网络接口配置...");
            netplanSyncService.syncInterfaces();
            log.info("网络接口配置同步完成");
        } catch (Exception e) {
            log.info("启动时同步网络接口配置失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
