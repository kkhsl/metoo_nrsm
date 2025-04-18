package com.metoo.nrsm.core.network.snmp4j.response;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.snmp4j.PDU;

import java.util.HashMap;
import java.util.Map;

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

            if (parts.length < 16) {
                // 处理异常：OID 字段不足，无法解析 IPv6
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            String[] ipv6Parts = new String[16];
            int startIndex = parts.length - 16;  // 从后往前取16位
            System.arraycopy(parts, startIndex, ipv6Parts, 0, 16);

            // **转换 IPv6 地址格式**
            StringBuilder rawIPv6 = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                int part1 = Integer.parseInt(ipv6Parts[i]);
                int part2 = Integer.parseInt(ipv6Parts[i + 1]);
                rawIPv6.append(String.format("%02x%02x", part1, part2));
                if (i < 14) rawIPv6.append(":");
            }

            // 压缩 IPv6 地址
            String compressedIPv6 = compressIPv6(rawIPv6.toString());

            // 处理 MAC 地址
            if (mac.isEmpty()) {
                System.err.println("Warning: Missing MAC for IPv6: " + compressedIPv6);
                mac = "UNKNOWN";
            }

            arpResult.put(compressedIPv6, mac.toUpperCase());
        }

        return arpResult;
    }

    public static JSONArray parseDeviceArpV6(Map<String, String> arpV6Map, JSONObject portJson) {
        JSONArray resultArray = new JSONArray();

        for (Map.Entry<String, String> entry : arpV6Map.entrySet()) {
            String oid = entry.getKey();
            String rawMac = entry.getValue();

            // 1. 提取索引和IPv6地址部分
            String ipSegment = oid.replace("1.3.6.1.2.1.55.1.12.1.2.", "");
            String[] parts = ipSegment.split("\\.");

            // 2. 验证OID结构
            if (parts.length < 16) {
                // 处理异常：OID 字段不足，无法解析 IPv6
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            // 3. 提取索引和端口
            String index = parts[0];
            String port = portJson.optString(index, "N/A");

            // 4. 构建IPv6地址
            StringBuilder ipv6Builder = new StringBuilder();
            for (int i=1; i<parts.length; i+=2) { // 跳过索引
                int high = Integer.parseInt(parts[i]);
                int low = Integer.parseInt(parts[i+1]);
                ipv6Builder.append(String.format("%02x%02x", high, low));
                if (i < parts.length-2) ipv6Builder.append(":");
            }
            String ip = compressIPv6(ipv6Builder.toString());

            // 5. 过滤链路本地地址
            if (ip.startsWith("fe80:")) continue;


            // 7. 构建结果对象
            JSONObject entry1 = new JSONObject();
            entry1.put("ip", ip);
            entry1.put("mac", rawMac.toUpperCase());
            entry1.put("port", port);
            resultArray.put(entry1);
        }

        return resultArray;
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

            if (parts.length < 16) {
                // 处理异常：OID 字段不足，无法解析 IPv6
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            String[] ipv6Parts = new String[16];
            int startIndex = parts.length - 16;  // 从后往前取16位
            System.arraycopy(parts, startIndex, ipv6Parts, 0, 16);

            // **转换 IPv6 地址格式**
            StringBuilder rawIPv6 = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                int part1 = Integer.parseInt(ipv6Parts[i]);
                int part2 = Integer.parseInt(ipv6Parts[i + 1]);
                rawIPv6.append(String.format("%02x%02x", part1, part2));
                if (i < 14) rawIPv6.append(":");
            }

            // 压缩 IPv6 地址
            String compressedIPv6 = compressIPv6(rawIPv6.toString());

            // **存入结果映射**
            portV6Result.put(compressedIPv6, port);
        }

        return portV6Result;
    }

    public static JSONArray parseDevicePortV6(Map<String, String> arpV6Map,
                                              JSONObject portJson,
                                              JSONObject statusJson,
                                              JSONObject descriptionJson) {
        JSONArray resultArray = new JSONArray();

        for (Map.Entry<String, String> entry : arpV6Map.entrySet()) {
            String oid = entry.getKey();
            String rawValue = entry.getValue(); // 格式应为 "INTEGER: 64"

            // 1. 提取索引和IPv6地址部分
            String ipSegment = oid.replace("1.3.6.1.2.1.55.1.8.1.2.", "");
            String[] parts = ipSegment.split("\\.");

            // 2. 验证OID结构
            if (parts.length < 16) {
                // 处理异常：OID 字段不足，无法解析 IPv6
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            // 3. 提取索引
            String index = parts[0];

            // 4. 获取端口信息
            String port = portJson.optString(index, "N/A");
            String status = statusJson.optString(index, "unknown");
            String description = descriptionJson.optString(index, "");

            // 5. 解析掩码长度（前缀）
            String prefixLength = rawValue.replaceAll("\\D+", ""); // 提取数字部分

            // 6. 构建IPv6地址
            StringBuilder ipv6Builder = new StringBuilder();
            for (int i = 1; i < 17; i += 2) { // 处理16字节（索引后的16个元素）
                int high = Integer.parseInt(parts[i]);
                int low = Integer.parseInt(parts[i + 1]);
                ipv6Builder.append(String.format("%02x%02x", high, low));
                if (i < 15) ipv6Builder.append(":"); // 共8段，最后不加冒号
            }
            String ip = compressIPv6(ipv6Builder.toString());

            // 7. 过滤链路本地地址
            if (ip.startsWith("fe80:")) continue;

            // 8. 构建结果对象
            JSONObject entryObj = new JSONObject();
            entryObj.put("port", port);
            entryObj.put("status", status);
            entryObj.put("ip", ip);
            entryObj.put("mask", prefixLength);
            entryObj.put("description", description);

            resultArray.put(entryObj);
        }

        return resultArray;
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

    public static Map<String, String> parseDeviceMacType(Map<String, String> macMap,String str) {
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

    public static String convertOidToMac(String oid) {
        String[] parts = oid.split("\\.");
        StringBuilder macAddress = new StringBuilder();
        for (int i = 0; i < parts.length; i++) { // 从索引 0 开始遍历
            int octet = Integer.parseInt(parts[i]);
            macAddress.append(String.format("%02x", octet));
            if (i < parts.length - 1) {
                macAddress.append(":");
            }
        }
        return macAddress.toString().toLowerCase();
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
        return false;
    }

    public static Map<String, String> parseTraffic(
            PDU inData,
            PDU outData) {
        String in = null;
        String out = null;
        Map<String, String> result = new HashMap<>(2);
        if (inData != null && !inData.getVariableBindings().isEmpty()) {
            in = inData.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("\"", "");
        }
        if (outData != null && !outData.getVariableBindings().isEmpty()) {
            out = outData.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("\"", "");
        }
        // 入口流量求和
        result.put("in",in);
        // 出口流量求和
        result.put("out",out);
        return result;
    }


    /**
     * 压缩 IPv6 地址（符合 RFC 5952 标准）
     * 示例输入：fe80000000000000025056fffe8683a2
     * 输出：fe80::250:56ff:fe86:83a2
     */
    private static String compressIPv6(String rawIPv6) {
        String[] blocks = rawIPv6.split(":");

        // 1. 将全零块转换为单个 0
        for (int i = 0; i < blocks.length; i++) {
            if ("0000".equals(blocks[i])) {
                blocks[i] = "0";
            } else {
                // 去除前导零（但至少保留一个字符）
                blocks[i] = blocks[i].replaceFirst("^0+(?!$)", "");
            }
        }

        // 2. 查找最长的连续零块
        int maxZeroStart = -1;
        int maxZeroLength = 0;
        int currentZeroStart = -1;
        int currentZeroLength = 0;

        for (int i = 0; i < blocks.length; i++) {
            if ("0".equals(blocks[i]) || "0000".equals(blocks[i])) {
                if (currentZeroStart == -1) {
                    currentZeroStart = i;
                }
                currentZeroLength++;
            } else {
                if (currentZeroLength > maxZeroLength) {
                    maxZeroLength = currentZeroLength;
                    maxZeroStart = currentZeroStart;
                }
                currentZeroStart = -1;
                currentZeroLength = 0;
            }
        }

        // 检查末尾的零序列
        if (currentZeroLength > maxZeroLength) {
            maxZeroLength = currentZeroLength;
            maxZeroStart = currentZeroStart;
        }

        // 3. 构建压缩后的地址
        StringBuilder compressed = new StringBuilder();
        boolean hasCompressed = false;

        for (int i = 0; i < blocks.length; ) {
            if (i == maxZeroStart && maxZeroLength > 1) {
                compressed.append(i == 0 ? "::" : ":");
                hasCompressed = true;
                i += maxZeroLength;
            } else {
                if (i != 0) compressed.append(":");
                compressed.append(blocks[i]);
                i++;
            }
        }

        // 处理全零的特殊情况
        if (compressed.toString().equals("0:0:0:0:0:0:0:0")) {
            return "::";
        }

        // 处理开头或结尾的压缩标记
        String result = compressed.toString()
                .replaceAll("::+", "::")
                .replaceAll("^0::", "::")
                .replaceAll("::$", "::");

        return result;
    }



}
