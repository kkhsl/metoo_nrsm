package com.metoo.nrsm.core.system.service.command.impl;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

public class SystemctlStartCommand extends BaseSystemctlCommand {
    public SystemctlStartCommand(String serviceName) {
        super(serviceName);
    }

    @Override
    public ServiceInfo execute() throws ServiceOperationException {
        return executeCommand("start");
    }

    @Override
    public String getCommandName() {
        return "start";
    }
}