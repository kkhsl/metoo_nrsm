package com.metoo.nrsm.core.system.service.command.factory;

import com.metoo.nrsm.core.system.service.command.ServiceCommand;

/**
 * 服务命令工厂接口
 * 用于创建各种服务管理命令
 */
public interface ServiceCommandFactory {

    ServiceCommand createStatusCommand(String serviceName);

    ServiceCommand createStartCommand(String serviceName);

    ServiceCommand createStopCommand(String serviceName);

    ServiceCommand createRestartCommand(String serviceName);

    ServiceCommand createEnableCommand(String serviceName);

    ServiceCommand createDisableCommand(String serviceName);

}
