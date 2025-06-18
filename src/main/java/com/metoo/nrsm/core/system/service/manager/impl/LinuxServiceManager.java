package com.metoo.nrsm.core.system.service.manager.impl;

import com.metoo.nrsm.core.system.service.command.ServiceCommand;
import com.metoo.nrsm.core.system.service.command.factory.ServiceCommandFactory;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.ServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

/**
 * Linux系统服务管理器实现
 * 基于systemctl命令管理服务
 */
public class LinuxServiceManager implements ServiceManager {

    private final ServiceCommandFactory commandFactory;

    public LinuxServiceManager(ServiceCommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public ServiceInfo getStatus(String serviceName) throws ServiceOperationException {
        ServiceCommand command = commandFactory.createStatusCommand(serviceName);
        return command.execute();
    }

    @Override
    public ServiceInfo startService(String serviceName) throws ServiceOperationException {
        ServiceCommand command = commandFactory.createStartCommand(serviceName);
        return command.execute();
    }

    @Override
    public ServiceInfo stopService(String serviceName) throws ServiceOperationException {
        ServiceCommand command = commandFactory.createStopCommand(serviceName);
        return command.execute();
    }

    @Override
    public ServiceInfo restartService(String serviceName) throws ServiceOperationException {
        ServiceCommand command = commandFactory.createRestartCommand(serviceName);
        return command.execute();
    }

    @Override
    public ServiceInfo enableService(String serviceName) throws ServiceOperationException {
        ServiceCommand command = commandFactory.createEnableCommand(serviceName);
        return command.execute();
    }

    @Override
    public ServiceInfo disableService(String serviceName) throws ServiceOperationException {
        ServiceCommand command = commandFactory.createDisableCommand(serviceName);
        return command.execute();
    }
}