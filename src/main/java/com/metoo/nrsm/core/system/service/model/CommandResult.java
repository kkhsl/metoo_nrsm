package com.metoo.nrsm.core.system.service.model;

public class CommandResult {
    private final String output;
    private final int exitStatus;

    public CommandResult(String output, int exitStatus) {
        this.output = output;
        this.exitStatus = exitStatus;
    }

    public String getOutput() {
        return output;
    }

    public int getExitStatus() {
        return exitStatus;
    }
}
