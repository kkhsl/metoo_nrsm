package com.metoo.nrsm.core.network.snmp4j.response;

import org.snmp4j.PDU;

/**
 * 考虑到需要处理的数据较少，先讲处理snmp返回数据的方法，放到这一个类中
 */
public class SNMPDataParser {

    // 解析 SNMP 响应，获取设备名
    public static String parseDeviceName(PDU responsePdu) {
        if (responsePdu != null && !responsePdu.getVariableBindings().isEmpty()) {
            String result = responsePdu.getVariableBindings().firstElement().toString();
            return result.split("=")[1].trim().replace("STRING: ", "").replace("\"", "");
        }
        return null;
    }
}
