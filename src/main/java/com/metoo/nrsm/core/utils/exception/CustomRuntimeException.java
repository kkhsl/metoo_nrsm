package com.metoo.nrsm.core.utils.exception;

public class CustomRuntimeException extends RuntimeException {

    private int code;  // 错误代码
    private String message;  // 错误消息

    // 无参构造函数
    public CustomRuntimeException() {
        super();
    }

    // 带错误消息的构造函数
    public CustomRuntimeException(String message) {
        super(message);
        this.message = message;
    }

    // 带错误代码和错误消息的构造函数
    public CustomRuntimeException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    // 获取错误代码
    public int getCode() {
        return code;
    }

    // 设置错误代码
    public void setCode(int code) {
        this.code = code;
    }

    // 获取错误消息
    @Override
    public String getMessage() {
        return message;
    }

    // 设置错误消息
    public void setMessage(String message) {
        this.message = message;
    }
}
