package com.metoo.nrsm.core.manager.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class VerifyMacVendorUtils {

    private static final Map<String, String> VENDOR_MAP_DEVICESCAN = new HashMap<>();

    static {
        VENDOR_MAP_DEVICESCAN.put("Tenda", "Tenda");
        VENDOR_MAP_DEVICESCAN.put("h3c", "h3c");
        VENDOR_MAP_DEVICESCAN.put("Ruijie", "Ruijie");
        VENDOR_MAP_DEVICESCAN.put("TP-LINK", "TP-LINK");
        VENDOR_MAP_DEVICESCAN.put("mercury", "mercury");
        VENDOR_MAP_DEVICESCAN.put("Huawei Device", "Huawei Device");
        VENDOR_MAP_DEVICESCAN.put("ruijie", "ruijie");
        VENDOR_MAP_DEVICESCAN.put("PUTIAN", "PUTIAN");
        VENDOR_MAP_DEVICESCAN.put("Sundray", "Sundray");
        VENDOR_MAP_DEVICESCAN.put("Topsec", "Topsec");
        VENDOR_MAP_DEVICESCAN.put("Sangfor", "Sangfor");
        VENDOR_MAP_DEVICESCAN.put("Xunte", "Xunte");
        VENDOR_MAP_DEVICESCAN.put("DPtech", "DPtech");
        VENDOR_MAP_DEVICESCAN.put("D-Link", "D-Link");
        VENDOR_MAP_DEVICESCAN.put("NETGEAR", "NETGEAR");
        VENDOR_MAP_DEVICESCAN.put("NETCORE", "NETCORE");
        VENDOR_MAP_DEVICESCAN.put("zte", "zte");
        VENDOR_MAP_DEVICESCAN.put("Fiberhome", "Fiberhome");
        VENDOR_MAP_DEVICESCAN.put("Ericsson", "Ericsson");
        VENDOR_MAP_DEVICESCAN.put("Cisco", "Cisco");
        VENDOR_MAP_DEVICESCAN.put("Juniper", "Juniper");
        VENDOR_MAP_DEVICESCAN.put("Brocade", "Brocade");
        VENDOR_MAP_DEVICESCAN.put("Extreme", "Extreme");
        VENDOR_MAP_DEVICESCAN.put("ProCurve", "ProCurve");
        VENDOR_MAP_DEVICESCAN.put("Maipu", "Maipu");
        VENDOR_MAP_DEVICESCAN.put("Venustech", "Venustech");
        ;
        VENDOR_MAP_DEVICESCAN.put("SHENZHEN FAST", "SHENZHEN FAST");
    }

    public static String toDevice(String vendor) {
        if (StringUtils.isNotEmpty(vendor)) {
            String lowerVendor = vendor.toLowerCase();
            for (Map.Entry<String, String> entry : VENDOR_MAP_DEVICESCAN.entrySet()) {
                if (lowerVendor.contains(entry.getKey().toLowerCase())) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

}
