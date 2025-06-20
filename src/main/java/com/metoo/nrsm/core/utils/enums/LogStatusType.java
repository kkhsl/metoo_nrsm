package com.metoo.nrsm.core.utils.enums;

/**
 * 采集日志结果类型枚举类
 *
 * @author zzy
 */
public enum LogStatusType {
    init(1, "采集中"),
    SUCCESS(2, "采集成功"),
    FAIL(3, "采集失败");

    private Integer code;

    private String value;

    private LogStatusType(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public static String getValueByCode(Integer code) {
        for (LogStatusType type : LogStatusType.values()) {
            if (type.getCode().equals(code)) {
                return type.getValue();
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
