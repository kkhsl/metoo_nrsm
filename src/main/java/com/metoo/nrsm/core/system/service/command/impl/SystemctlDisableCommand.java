package com.metoo.nrsm.core.system.service.command.impl;


import com.metoo.nrsm.core.system.service.exception.ServiceOperationException;
import com.metoo.nrsm.core.system.service.model.ServiceInfo;

public class SystemctlDisableCommand extends BaseSystemctlCommand {

    public SystemctlDisableCommand(String serviceName) {
        super(serviceName);
    }

    @Override
    public ServiceInfo execute() throws ServiceOperationException {
        return executeCommand("disable");
    }

    @Override
    public String getCommandName() {
        return "disable";
    }
}