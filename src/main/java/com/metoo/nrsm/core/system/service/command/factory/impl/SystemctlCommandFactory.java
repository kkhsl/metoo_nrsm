package com.metoo.nrsm.core.system.service.command.factory.impl;

import com.metoo.nrsm.core.system.service.command.ServiceCommand;
import com.metoo.nrsm.core.system.service.command.factory.ServiceCommandFactory;
import com.metoo.nrsm.core.system.service.command.impl.*;

/**
 * systemctl命令工厂实现
 * 创建各种基于systemctl的服务管理命令
 */
public class SystemctlCommandFactory implements ServiceCommandFactory {

    @Override
    public ServiceCommand createStatusCommand(String serviceName) {
        return new SystemctlStatusCommand(serviceName);
    }

    @Override
    public ServiceCommand createStartCommand(String serviceName) {
        return new SystemctlStartCommand(serviceName);
    }

    @Override
    public ServiceCommand createStopCommand(String serviceName) {
        return new SystemctlStopCommand(serviceName);
    }

    @Override
    public ServiceCommand createRestartCommand(String serviceName) {
        return new SystemctlRestartCommand(serviceName);
    }

    @Override
    public ServiceCommand createEnableCommand(String serviceName) {
        return new SystemctlEnableCommand(serviceName);
    }

    @Override
    public ServiceCommand createDisableCommand(String serviceName) {
        return new SystemctlDisableCommand(serviceName);
    }
}