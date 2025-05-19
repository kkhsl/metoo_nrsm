package com.metoo.nrsm.core.system.service.exception;

/**
 * 服务操作异常类
 * 封装服务管理过程中可能出现的异常
 */
public class ServiceOperationException extends Exception {

    public ServiceOperationException(String message) {
        super(message);
    }

    public ServiceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}