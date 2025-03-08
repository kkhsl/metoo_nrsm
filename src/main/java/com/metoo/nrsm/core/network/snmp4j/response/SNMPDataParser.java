package com.metoo.nrsm.core.network.snmp4j.response;

import com.google.gson.Gson;
import com.metoo.nrsm.core.network.snmp4j.constants.SNMP_OID;
import org.snmp4j.PDU;

import java.util.*;

/**
 * 考虑到需要处理的数据较少，先讲处理snmp返回数据的方法，放到这一个类中
 */
public class SNMPDataParser {
    private static final Gson gson = new Gson();
    public static String convertToJson(Map<String, String> arpResult) {
        Gson gson = new Gson();
        return gson.toJson(arpResult);
    }

    // 解析 SNMP 响应，获取设备名
    public static String parseDeviceName(PDU responsePdu) {
        if (responsePdu != null && !responsePdu.getVariableBindings().isEmpty()) {
            String result = responsePdu.getVariableBindings().firstElement().toString();
            return result.split("=")[1].trim().replace("STRING: ", "").replace("\"", "");
        }
        return null;
    }

    // 解析 ARP 表的逻辑，转换为 Map<String, String> 格式
    public static Map<String, String> parseDeviceArp(Map<String, String> arpMap) {
        Map<String, String> arpResult = new HashMap<>();

        // 遍历每一条 OID 数据
        for (Map.Entry<String, String> entry : arpMap.entrySet()) {
            String oid = entry.getKey();
            String mac = entry.getValue();
            String ip = oid.replace("1.3.6.1.2.1.4.22.1.2.", "");
            String[] ipParts = ip.split("\\.");
            if (ipParts.length == 5) {
                String ipAddress = String.join(".", ipParts[1], ipParts[2], ipParts[3], ipParts[4]);
                arpResult.put(ipAddress, mac);
            } else {
                // 如果 IP 地址格式不正确，输出错误
                System.err.println("Error: IP format is not correct in OID: " + oid);
            }
        }
        return arpResult;
    }


    // 解析 ArpPort 表的逻辑，转换为 Map<String, String> 格式
    public static Map<String, String> parseDeviceArpPort(Map<String, String> arpPortMap) {
        Map<String, String> arpResult = new HashMap<>();

        // 遍历每一条 OID 数据
        for (Map.Entry<String, String> entry : arpPortMap.entrySet()) {
            String oid = entry.getKey();
            String port = entry.getValue();
            String ip = oid.replace("1.3.6.1.2.1.4.22.1.1.", "");
            // 格式化 IP 地址：这里假设处理规则是去除 OID 前缀后得到的 IP 地址格式
            String[] ipParts = ip.split("\\.");
            if (ipParts.length == 5) {
                // 直接采用从 OID 提取的 IP 地址
                String ipAddress = String.join(".", ipParts[1], ipParts[2], ipParts[3], ipParts[4]);
                // 将 IP 地址和端口加入结果 Map
                arpResult.put(ipAddress, port);
            } else {
                // 如果 IP 地址格式不正确，输出错误
                System.err.println("Error: IP format is not correct in OID: " + oid);
            }
        }

        return arpResult;
    }

    // 解析 ArpV6 表的逻辑，转换为 Map<String, String> 格式
    public static Map<String, String> parseDeviceArpV6(Map<String, String> arpV6Map) {
        Map<String, String> arpResult = new HashMap<>();

        for (Map.Entry<String, String> entry : arpV6Map.entrySet()) {
            String oid = entry.getKey();
            String mac = entry.getValue();
            String ipSegment = oid.replace("1.3.6.1.2.1.55.1.12.1.2.", "");

            // 分割 OID 剩余部分
            String[] parts = ipSegment.split("\\.");

            if (parts.length < 18) { // 至少应有 18 个部分（去掉前两个后仍需 16 个）
                System.err.println("Error: Invalid IPv6 OID format: " + oid);
                continue;
            }

            // **移除前两个数值**（例如 `193` 和 `16`）
            String[] ipv6Parts = new String[16];
            System.arraycopy(parts, 2, ipv6Parts, 0, 16);

            // **转换 IPv6 地址格式**
            StringBuilder ipv6Address = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                int part1 = Integer.parseInt(ipv6Parts[i]);
                int part2 = Integer.parseInt(ipv6Parts[i + 1]);
                String hexPart = String.format("%02x%02x", part1, part2);
                ipv6Address.append(hexPart);
                if (i < 14) ipv6Address.append(":");
            }

            String formattedIPv6 = ipv6Address.toString();

            // **处理 MAC 地址，确保格式正确**
            if (mac.isEmpty()) {
                System.err.println("Warning: Missing MAC for IPv6: " + formattedIPv6);
                mac = "UNKNOWN";
            }
            // **存入结果映射**
            arpResult.put(formattedIPv6, mac);
        }

        return arpResult;
    }

    // 解析 ArpV6Port 表的逻辑，转换为 Map<String, String> 格式
    public static String parseDeviceArpV6Port(PDU responsePdu) {
        return null;
    }

    public static Map<String, String> parseDevicePort(Map<String, String> portMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : portMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String portName = entry.getValue(); // 端口名称
            // 获取最后一部分
            String[] oidParts = oid.split("\\.");
            String lastPart = oidParts[oidParts.length - 1]; // 取最后一位
            // 构建结果
            result.put(lastPart, portName);
        }
        return result;
    }
    public static Map<String, String> parseDevicePortIp(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String portValue = entry.getValue(); // 端口值
            // 获取IP地址部分
            String ipAddress = oid.replace("1.3.6.1.2.1.4.20.1.2.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }
    public static Map<String, String> parseDevicePortV6(Map<String, String> arpV6Map) {
        Map<String, String> portV6Result = new HashMap<>();

        for (Map.Entry<String, String> entry : arpV6Map.entrySet()) {
            String oid = entry.getKey();
            String port = entry.getValue();
            String ipSegment = oid.replace("1.3.6.1.2.1.55.1.8.1.2.", "");

            // 分割 OID 剩余部分
            String[] parts = ipSegment.split("\\.");

            if (parts.length < 18) { // 至少应有 18 个部分（去掉前两个后仍需 16 个）
                System.err.println("Error: Invalid IPv6 OID format: " + oid);
                continue;
            }

            // **移除前两个数值**（例如 `193` 和 `16`）
            String[] ipv6Parts = new String[16];
            System.arraycopy(parts, 2, ipv6Parts, 0, 16);

            // **转换 IPv6 地址格式**
            StringBuilder ipv6Address = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                int part1 = Integer.parseInt(ipv6Parts[i]);
                int part2 = Integer.parseInt(ipv6Parts[i + 1]);
                String hexPart = String.format("%02x%02x", part1, part2);
                ipv6Address.append(hexPart);
                if (i < 14) ipv6Address.append(":");
            }

            String formattedIPv6 = ipv6Address.toString();
            // **存入结果映射**
            portV6Result.put(formattedIPv6, port);
        }

        return portV6Result;
    }



    public static Map<String, String> parseDevicePortMask(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String portValue = entry.getValue(); // 端口值
            // 获取IP地址部分
            String ipAddress = oid.replace("1.3.6.1.2.1.4.20.1.3.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }
    public static Map<String, String> parseDevicePortDescription(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String portValue = entry.getValue(); // 端口值
            // 获取IP地址部分
            String ipAddress = oid.replace("1.3.6.1.2.1.31.1.1.1.18.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }

    public static Map<String, String> parseDeviceMac(Map<String, String> macMap,String str) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : macMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String index = entry.getValue(); // 端口值

            String newOid = oid.replace(str, "");
            // 提取 MAC 地址部分
            String macAddress = convertOidToMac(newOid);
            result.put(macAddress, index);
        }

        return result;
    }

    private static String convertOidToMac(String oid) {
        String[] parts = oid.split("\\.");
        StringBuilder macAddress = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            int octet = Integer.parseInt(parts[i]);
            macAddress.append(String.format("%02x", octet));
            if (i < parts.length - 1) {
                macAddress.append(":");
            }
        }
        return macAddress.toString().toUpperCase();
    }


    public static Map<String, String> parsePortStatus(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String portValue = entry.getValue(); // 端口值
            String ipAddress = oid.replace("1.3.6.1.2.1.2.2.1.8.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }

    public static Map<String, String> parsePortMac(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey();
            String portValue = entry.getValue().toUpperCase();
            String ipAddress = oid.replace("1.3.6.1.2.1.2.2.1.6.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }


    public static String parseDeviceUpdateTime(PDU responsePdu) {
        if (responsePdu != null && !responsePdu.getVariableBindings().isEmpty()) {
            // 获取第一个变量绑定
            String result = responsePdu.getVariableBindings().firstElement().toString();
            // 提取 '=' 后的值
            String timePart = result.split("=")[1].trim();
            // 返回时间部分
            return timePart;
        }
        return null;
    }


    public static Map<String, String> parseLLDP(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey();
            String portValue = entry.getValue();
            String ipAddress = oid.replace("1.0.8802.1.1.2.1.4.1.1.9.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }

    public static Map<String, String> parseLLDPPort(Map<String, String> portIpMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : portIpMap.entrySet()) {
            String oid = entry.getKey();
            String portValue = entry.getValue().trim().replace(" ", "");
            String ipAddress = oid.replace("1.0.8802.1.1.2.1.4.1.1.7.", ""); // 移除 OID 部分
            // 构建结果
            result.put(ipAddress, portValue);
        }
        return result;
    }

    public static Boolean parseIsV6(PDU responsePdu) {
        if (responsePdu != null && !responsePdu.getVariableBindings().isEmpty()) {
            // 获取第一个变量绑定
            String result = responsePdu.getVariableBindings().firstElement().toString();
            // 检查输出
            if (result.contains("No Such Object")) {
                return false; // 设备不支持 IPv6
            } else {
                return true; // 设备支持 IPv6
            }
        }
        return null;
    }

}
