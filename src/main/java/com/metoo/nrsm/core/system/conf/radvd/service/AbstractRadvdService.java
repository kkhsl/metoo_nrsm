package com.metoo.nrsm.core.system.conf.radvd.service;

import com.metoo.nrsm.core.system.conf.radvd.strategy.RadvdConfigUpdateStrategy;
import com.metoo.nrsm.entity.Radvd;

import java.util.List;

public abstract class AbstractRadvdService {

    protected RadvdConfigUpdateStrategy updateStrategy;

    // 模板方法，定义更新配置文件的基本流程
    public void updateConfigFile(List<Radvd> radvdList) {
        // 步骤1: 获取配置信息
        List<Radvd> updatedConfigs = getUpdatedConfig(radvdList);

        // 步骤2: 使用具体策略更新配置文件
        updateStrategy.updateConfig(updatedConfigs);

        // 步骤3: 其他逻辑（如数据库同步等），可以在此实现
        afterConfigUpdated(updatedConfigs);
    }

    // 钩子方法，子类可以重写
    protected abstract List<Radvd> getUpdatedConfig(List<Radvd> radvdList);

    // 钩子方法，子类可以重写
    protected abstract void afterConfigUpdated(List<Radvd> updatedConfigs);
}