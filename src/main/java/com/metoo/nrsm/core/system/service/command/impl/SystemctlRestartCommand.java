package com.metoo.nrsm.core.system.service.command.impl;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

public class SystemctlRestartCommand extends BaseSystemctlCommand {

    public SystemctlRestartCommand(String serviceName) {
        super(serviceName);
    }

    @Override
    public ServiceInfo execute() throws ServiceOperationException {
        return executeCommand("restart");
    }

    @Override
    public String getCommandName() {
        return "restart";
    }
}