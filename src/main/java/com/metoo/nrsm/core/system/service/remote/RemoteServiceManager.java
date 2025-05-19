package com.metoo.nrsm.core.system.service.remote;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.manager.ServiceManager;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;
import com.metoo.nrsm.core.system.service.utils.ServiceInfoParser;

import java.io.ByteArrayInputStream;

/**
 * 远程服务管理器
 * 通过SSH远程执行systemctl命令管理服务
 */
public class RemoteServiceManager implements ServiceManager {
    private final SshCommandExecutor sshExecutor;
    private final ServiceInfoParser parser;

    public RemoteServiceManager(String host, int port, String username,
                                String password, int timeout) {
        this.sshExecutor = new SshCommandExecutor(host, port, username, password, timeout);
        this.parser = new ServiceInfoParser();
    }

    @Override
    public ServiceInfo getStatus(String serviceName) throws ServiceOperationException {
        try {
            String command = String.format("sudo systemctl status %s", serviceName);
            String output = sshExecutor.executeCommand(command);
            return parser.parse(new ByteArrayInputStream(output.getBytes()));
        } catch (Exception e) {
            throw new ServiceOperationException(
                    String.format("获取远程服务状态失败[服务:%s]", serviceName), e);
        }
    }

    @Override
    public ServiceInfo startService(String serviceName) throws ServiceOperationException {
        try {
            String command = String.format("sudo systemctl start %s", serviceName);
            sshExecutor.executeCommand(command);
            return getStatus(serviceName);
        } catch (Exception e) {
            throw new ServiceOperationException(
                    String.format("启动远程服务失败[服务:%s]", serviceName), e);
        }
    }

    @Override
    public ServiceInfo stopService(String serviceName) throws ServiceOperationException {
        try {
            String command = String.format("sudo systemctl stop %s", serviceName);
            sshExecutor.executeCommand(command);
            return getStatus(serviceName);
        } catch (Exception e) {
            throw new ServiceOperationException(
                    String.format("停止远程服务失败[服务:%s]", serviceName), e);
        }
    }

    @Override
    public ServiceInfo restartService(String serviceName) throws ServiceOperationException {
        try {
            String command = String.format("sudo systemctl restart %s", serviceName);
            sshExecutor.executeCommand(command);
            return getStatus(serviceName);
        } catch (Exception e) {
            throw new ServiceOperationException(
                    String.format("重启远程服务失败[服务:%s]", serviceName), e);
        }
    }

    @Override
    public ServiceInfo enableService(String serviceName) throws ServiceOperationException {
        try {
            String command = String.format("sudo systemctl enable %s", serviceName);
            sshExecutor.executeCommand(command);
            return getStatus(serviceName);
        } catch (Exception e) {
            throw new ServiceOperationException(
                    String.format("启用远程服务开机自启失败[服务:%s]", serviceName), e);
        }
    }

    @Override
    public ServiceInfo disableService(String serviceName) throws ServiceOperationException {
        try {
            String command = String.format("sudo systemctl disable %s", serviceName);
            sshExecutor.executeCommand(command);
            return getStatus(serviceName);
        } catch (Exception e) {
            throw new ServiceOperationException(
                    String.format("禁用远程服务开机自启失败[服务:%s]", serviceName), e);
        }
    }
}