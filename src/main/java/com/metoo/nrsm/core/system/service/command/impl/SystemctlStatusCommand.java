package com.metoo.nrsm.core.system.service.command.impl;

import com.metoo.nrsm.core.system.service.command.ServiceCommand;
import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

/**
 * 服务状态检查命令
 */
public class SystemctlStatusCommand extends BaseSystemctlCommand {

    public SystemctlStatusCommand(String serviceName) {
        super(serviceName);
    }

    @Override
    public ServiceInfo execute() throws ServiceOperationException {
        return executeCommand("status");
    }

    @Override
    public String getCommandName() {
        return "status";
    }
}
