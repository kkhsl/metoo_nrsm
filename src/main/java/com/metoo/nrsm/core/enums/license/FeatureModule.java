package com.metoo.nrsm.core.enums.license;

import java.util.Arrays;

public enum FeatureModule {

    ASSET_SCAN(0, "资产测绘"),
    TRAFFIC_ANALYSIS(1, "流量分析"),
    VULNERABILITY_SCAN(2, "漏洞扫描");

    private final int code;
    private final String desc;

    FeatureModule(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter
    public int getCode() { return code; }
    public String getDesc() { return desc; }

    // 通过code获取枚举
    public static FeatureModule of(int code) {
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效功能模块: " + code));
    }
}
