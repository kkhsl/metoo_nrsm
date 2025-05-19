package com.metoo.nrsm.core.system.service.command.impl;

import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

public class SystemctlEnableCommand extends BaseSystemctlCommand {
    public SystemctlEnableCommand(String serviceName) {
        super(serviceName);
    }

    @Override
    public ServiceInfo execute() throws ServiceOperationException {
        return executeCommand("enable");
    }

    @Override
    public String getCommandName() {
        return "enable";
    }
}