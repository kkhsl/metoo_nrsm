package com.metoo.nrsm.core.system.service.command.impl;


import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

public class SystemctlStopCommand extends BaseSystemctlCommand {

    public SystemctlStopCommand(String serviceName) {
        super(serviceName);
    }

    @Override
    public ServiceInfo execute() throws ServiceOperationException {
        return executeCommand("stop");
    }

    @Override
    public String getCommandName() {
        return "stop";
    }
}