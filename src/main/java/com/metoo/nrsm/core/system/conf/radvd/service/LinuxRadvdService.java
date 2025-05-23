package com.metoo.nrsm.core.system.conf.radvd.service;

import com.metoo.nrsm.core.system.conf.radvd.strategy.LinuxRadvdConfigUpdateStrategy;
import com.metoo.nrsm.entity.Radvd;

import java.util.List;

public class LinuxRadvdService extends AbstractRadvdService {

    public LinuxRadvdService() {
        this.updateStrategy = new LinuxRadvdConfigUpdateStrategy(); // 使用Linux更新策略
    }

    @Override
    protected List<Radvd> getUpdatedConfig(List<Radvd> radvdList) {
        // 从数据库获取最新配置
        // 这里假设您有一个方法来获取配置
        return radvdList; // 返回更新后的配置列表
    }

    @Override
    protected void afterConfigUpdated(List<Radvd> updatedConfigs) {
        // 执行其他操作，如记录日志或通知等
        System.out.println("配置文件已更新！Linux服务已同步配置文件。");
    }
}