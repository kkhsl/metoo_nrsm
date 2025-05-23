package com.metoo.nrsm.core.system.conf.network.sync.service;

import com.metoo.nrsm.core.system.conf.network.sync.LocalNetplanSyncService;
import com.metoo.nrsm.core.system.conf.network.sync.WindowsSshNetplanSyncService;
import com.metoo.nrsm.core.system.utils.OsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
public class AutoNetplanSyncService implements NetplanSyncService {

    private final OsHelper osHelper;
    private final LocalNetplanSyncService localSyncService;
    private final WindowsSshNetplanSyncService remoteSyncService;

    @Autowired
    public AutoNetplanSyncService(OsHelper osHelper,
                                  LocalNetplanSyncService localSyncService,
                                  WindowsSshNetplanSyncService remoteSyncService) {
        this.osHelper = osHelper;
        this.localSyncService = localSyncService;
        this.remoteSyncService = remoteSyncService;
    }

    @Override
    public void syncInterfaces() {
        if (osHelper.isLinux()) {
            log.info("linux同步接口");
            localSyncService.syncInterfaces();
        } else {
            log.info("windows同步接口");

            // Windows或其他系统使用远程SSH方式
            remoteSyncService.syncInterfaces();
        }
    }
}