package com.metoo.nrsm.core.enums.license;

import java.util.Arrays;

public enum BaseVersionType {

    GENERAL(0, "通用版本"),
    GOVERNMENT(1, "政务外网"),
    EDUCATION(2, "教体版");

    private final int code;
    private final String desc;

    BaseVersionType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter
    public int getCode() { return code; }
    public String getDesc() { return desc; }

    // 通过code获取枚举
    public static BaseVersionType of(int code) {
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效版本类型: " + code));
    }
}
