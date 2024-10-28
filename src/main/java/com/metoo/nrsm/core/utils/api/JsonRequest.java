package com.metoo.nrsm.core.utils.api;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-29 21:13
 */
public class JsonRequest {

    private String taskuuid;
    private String ip;
    private String port;

    public String getTaskuuid() {
        return taskuuid;
    }

    public void setTaskuuid(String taskuuid) {
        this.taskuuid = taskuuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
