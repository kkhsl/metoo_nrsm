package com.metoo.nrsm.core.system.conf.network.sync;

import org.springframework.beans.factory.annotation.Autowired;

public class NetworkConfigManager {

    // 本地执行
    @Autowired
    private LocalNetplanSyncService localSyncService;

    public void syncLocal() {
        localSyncService.syncInterfaces();
    }


    // 远程执行
    @Autowired
    private WindowsSshNetplanSyncService remoteSyncService;

    public void syncRemote() {
        remoteSyncService.syncInterfaces();
    }

    public static void main(String[] args) {

    }

}
