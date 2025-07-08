package com.metoo.nrsm.core.system.service.exception;

/**
 * 远程操作异常类
 * 封装远程服务管理过程中可能出现的异常
 */
public class RemoteOperationException extends Exception {
    /**
     * 基础构造方法
     *
     * @param message 异常信息
     */
    public RemoteOperationException(String message) {
        super(message);
    }

    /**
     * 带原因的构造方法
     *
     * @param message 异常信息
     * @param cause   原始异常
     */
    public RemoteOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}