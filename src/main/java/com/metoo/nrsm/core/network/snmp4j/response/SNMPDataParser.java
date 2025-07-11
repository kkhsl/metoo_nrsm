package com.metoo.nrsm.core.network.snmp4j.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snmp4j.PDU;

import java.util.*;

/**
 * 考虑到需要处理的数据较少，先讲处理snmp返回数据的方法，放到这一个类中
 */
public class SNMPDataParser {
//    private static final Gson gson = new Gson();
//    public static String convertToJson(Map<String, String> arpResult) {
//        Gson gson = new Gson();
//        return gson.toJson(arpResult);
//    }

    public static String convertToJson(Map<String, String> arpResult) {
        if (arpResult.isEmpty()) {
            return "";
        }
        JSONObject jsonObject = new JSONObject(arpResult);
        return jsonObject.toString();
    }

    public static String convertToJson(List<Map<String, String>> data) {
        if (data == null) {
            return "null";
        }

        try {
            if (data.isEmpty()) {
                return "[]";
            }

            // 创建JSON数组
            JSONArray jsonArray = new JSONArray();

            // 遍历每个路由条目
            for (Map<String, String> entry : data) {
                // 为每个条目创建JSON对象
                JSONObject entryObj = new JSONObject();

                // 添加所有键值对
                for (Map.Entry<String, String> field : entry.entrySet()) {
                    String key = field.getKey();
                    String value = field.getValue() != null ? field.getValue() : "";
                    entryObj.put(key, value);
                }

                // 将对象添加到数组
                jsonArray.put(entryObj);
            }

            return jsonArray.toString();
        } catch (JSONException e) {
            // 异常处理
            return null;
        }
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

            // 1. 直接分割整个 OID
            String[] oidParts = oid.split("\\.");

            // 2. 验证 OID 长度
            if (oidParts.length < 16) {
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            // 3. 提取最后16个字段（IPv6地址）
            StringBuilder rawIPv6 = new StringBuilder();
            int startIndex = oidParts.length - 16; // 最后16个字段的位置

            // 4. 转换每两个字段为一个 IPv6 段
            for (int i = startIndex; i < oidParts.length; i += 2) {
                int part1 = Integer.parseInt(oidParts[i]);
                int part2 = Integer.parseInt(oidParts[i + 1]);
                rawIPv6.append(String.format("%02x%02x", part1, part2));
                if (i < oidParts.length - 2) {
                    rawIPv6.append(":");
                }
            }

            // 5. 压缩 IPv6 地址
            String compressedIPv6 = compressIPv6(rawIPv6.toString());

            // 6. 处理 MAC 地址
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

            // 1. 直接分割整个 OID
            String[] oidParts = oid.split("\\.");

            // 2. 验证 OID 长度（需要足够的字段）
            if (oidParts.length < 17) { // 17 = 前缀部分 + 16字节
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            // 3. 提取设备索引（IPv6地址前的数字）
            String deviceIndex = oidParts[oidParts.length - 17]; // 从后往前第17个部分

            // 4. 从最后16个字段提取 IPv6 地址
            StringBuilder ipv6Builder = new StringBuilder();
            int startIndex = oidParts.length - 16;
            for (int i = startIndex; i < oidParts.length; i += 2) {
                int high = Integer.parseInt(oidParts[i]);
                int low = Integer.parseInt(oidParts[i + 1]);
                ipv6Builder.append(String.format("%02x%02x", high, low));
                if (i < oidParts.length - 2) {
                    ipv6Builder.append(":");
                }
            }
            String ip = compressIPv6(ipv6Builder.toString());

            // 5. 获取端口信息
            String port = portJson.optString(deviceIndex, "N/A");

            // 6. 构建结果对象（保留所有地址）
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

            // 1. 直接分割整个 OID
            String[] oidParts = oid.split("\\.");

            // 2. 验证 OID 长度
            if (oidParts.length < 16) {
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            // 3. 提取最后16个字段（IPv6地址）
            StringBuilder rawIPv6 = new StringBuilder();
            int startIndex = oidParts.length - 16; // 最后16个字段的起始位置

            // 4. 转换每两个字段为一个 IPv6 段
            for (int i = startIndex; i < oidParts.length; i += 2) {
                int part1 = Integer.parseInt(oidParts[i]);
                int part2 = Integer.parseInt(oidParts[i + 1]);
                rawIPv6.append(String.format("%02x%02x", part1, part2));
                if (i < oidParts.length - 2) {
                    rawIPv6.append(":");
                }
            }

            // 5. 压缩 IPv6 地址
            String compressedIPv6 = compressIPv6(rawIPv6.toString());

            // 6. 存入结果映射
            portV6Result.put(compressedIPv6, port);
        }

        return portV6Result;
    }

    public static JSONArray parseDevicePortV6(Map<String, String> arpV6Map,
                                              JSONObject portJson) {
        JSONArray resultArray = new JSONArray();

        for (Map.Entry<String, String> entry : arpV6Map.entrySet()) {
            String oid = entry.getKey();
            String rawValue = entry.getValue(); // 格式应为 "INTEGER: 64"

            // 1. 提取OID末尾的16个数字部分（IPv6地址的16个字节）
            String[] oidParts = oid.split("\\.");
            if (oidParts.length < 17) {
                System.err.println("Invalid OID format: " + oid);
                continue;
            }

            // 2. 提取最后16个字节（索引长度-16到末尾）
            int startIndex = oidParts.length - 16;
            StringBuilder ipv6Builder = new StringBuilder();
            for (int i = startIndex; i < oidParts.length; i += 2) {
                int high = Integer.parseInt(oidParts[i]);
                int low = Integer.parseInt(oidParts[i + 1]);
                ipv6Builder.append(String.format("%02x%02x", high, low));
                if (i < oidParts.length - 2) {
                    ipv6Builder.append(":");
                }
            }

            // 3. 获取设备索引（OID中IPv6之前的数字）
            String deviceIndex = oidParts[oidParts.length - 17];

            // 4. 获取端口信息和前缀长度
            String port = portJson.optString(deviceIndex, "N/A");
            String prefixLength = rawValue.replaceAll("\\D+", "");

            // 5. 构建完整IPv6地址（压缩格式）
            String ip = compressIPv6(ipv6Builder.toString());

            // 6. 创建结果对象（保留所有地址）
            JSONObject entryObj = new JSONObject();
            entryObj.put("port", port);
            entryObj.put("ipv6", ip + "/" + prefixLength);

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

    public static Map<String, String> parseDeviceMac(Map<String, String> macMap, String str) {
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

    public static Map<String, String> parseDeviceMacType(Map<String, String> macMap, String str) {
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


    public static List<Map<String, String>> parseRoute(
            Map<String, String> destNetworkMap,
            Map<String, String> costMap,
            Map<String, String> protoTypeMap,
            Map<String, String> portMap) {

        // 存储所有路由条目的列表
        List<Map<String, String>> routeEntries = new ArrayList<>();

        // 遍历 destNetworkMap 中的每个条目
        for (Map.Entry<String, String> entry : destNetworkMap.entrySet()) {
            try {
                // 获取 OID 的尾部特征（用于匹配）
                String oidSuffix = extractOIDSuffix(entry.getKey());
                if (oidSuffix == null || oidSuffix.isEmpty()) {
                    continue; // 跳过无效条目
                }

                Map<String, String> route = new HashMap<>();

                // 解析目标网络、掩码和下一跳地址
                parseNetInfo(oidSuffix, route);

                String interfaceIndex = entry.getValue();
                route.put("Interface", interfaceIndex);
                route.put("Port", portMap.getOrDefault(interfaceIndex, "unknown"));

                String cost = matchBySuffix(costMap, oidSuffix);
                route.put("Cost", cost);

                // 匹配路由类型
                String typeCode = matchBySuffix(protoTypeMap, oidSuffix);
                route.put("Preference", typeCode);
                route.put("type", getRouteType(typeCode));

                routeEntries.add(route);
            } catch (Exception e) {
                System.err.println("处理路由条目失败: " + entry.getKey());
                e.printStackTrace();
            }
        }

        return routeEntries;
    }

    /**
     * 提取 OID 尾部特征
     */
    private static String extractOIDSuffix(String oidKey) {
        // 截取 OID 的尾部部分
        int prefixLength = "1.3.6.1.2.1.4.24.4.1.5".length() + 1;
        if (oidKey.length() > prefixLength) {
            return oidKey.substring(prefixLength);
        }
        return "";
    }

    /**
     * 解析目标网络、掩码和下一跳地址
     */
    private static void parseNetInfo(String oidSuffix, Map<String, String> route) {
        String[] parts = oidSuffix.split("\\.");

        try {
            // 目标网络
            StringBuilder destNetwork = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i > 0) destNetwork.append(".");
                destNetwork.append(parts[i]);
            }
            route.put("Destnetwork", destNetwork.toString());

            // 子网掩码
            StringBuilder mask = new StringBuilder();
            for (int i = 4; i < 8; i++) {
                if (i > 4) mask.append(".");
                mask.append(parts[i]);
            }
            route.put("Mask", mask.toString());

            // 下一跳地址
            StringBuilder nextHop = new StringBuilder();
            for (int i = 9; i < 13; i++) { // 跳过第8位
                if (i > 9) nextHop.append(".");
                nextHop.append(parts[i]);
            }
            route.put("Nexthop", nextHop.toString());

        } catch (IndexOutOfBoundsException e) {
            System.err.println("无法解析 OID 后缀: " + oidSuffix);
        }
    }

    /**
     * 通过后缀匹配值
     */
    private static String matchBySuffix(Map<String, String> map, String suffix) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            // 检查是否以相同后缀结尾
            if (entry.getKey().endsWith(suffix)) {
                return entry.getValue();
            }
        }
        return "N/A";
    }


    public static List<Map<String, String>> parseRoute6(
            Map<String, String> destNetworkMap,
            Map<String, String> nextHopMap,
            Map<String, String> costMap,
            Map<String, String> protoTypeMap,
            Map<String, String> portMap,
            Map<String, String> preferenceMap) {

        List<Map<String, String>> routeEntries = new ArrayList<>();

        // 1. 创建标识符到各属性的映射
        Map<String, String> suffixToCost = new HashMap<>();
        Map<String, String> suffixToProto = new HashMap<>();
        Map<String, String> suffixToPreference = new HashMap<>();
        Map<String, String> suffixToInterface = new HashMap<>();
        Map<String, String> suffixToOid = new HashMap<>();
        Map<String, String> suffixToNextHop = new HashMap<>();

        // 预处理数据索引
        createSuffixIndex(costMap, suffixToCost);
        createSuffixIndex(protoTypeMap, suffixToProto);
        createSuffixIndex(preferenceMap, suffixToPreference);
        createSuffixIndex(destNetworkMap, suffixToOid);
        createSuffixIndex(nextHopMap, suffixToNextHop);

        // 2. 创建接口映射
        for (Map.Entry<String, String> entry : destNetworkMap.entrySet()) {
            String suffix = getOidSuffix(entry.getKey());
            if (suffix != null) {
                suffixToInterface.put(suffix, entry.getValue());
            }
        }

        // 3. 处理每条路由
        for (Map.Entry<String, String> destEntry : destNetworkMap.entrySet()) {
            try {
                String oid = destEntry.getKey();
                String suffix = getOidSuffix(oid);
                if (suffix == null) continue;

                Map<String, String> route = new LinkedHashMap<>();

                // 解析目的网络和前缀长度
                String[] parts = oid.split("\\.");
                int startIdx = parts.length - 18;  // 前16位+前缀长度+后缀
                if (startIdx < 0) startIdx = 0;

                // 提取目的网络地址
                StringBuilder destNetwork = new StringBuilder();
                for (int i = startIdx; i < startIdx + 16 && i < parts.length - 2; i++) {
                    if (destNetwork.length() > 0) destNetwork.append(":");
                    destNetwork.append(parts[i]);
                }

                // 提取前缀长度 (倒数第二位)
                String prefixLength = "64";
                if (parts.length >= 2) {
                    prefixLength = parts[parts.length - 2];
                }

                // 获取接口信息
                String ifIndex = destNetworkMap.get(oid);
                String portName = portMap.getOrDefault(ifIndex, "unknown");

                // 添加路由信息
                route.put("Destnetwork", convertToIPv6(destNetwork.toString()));
                route.put("Mask", prefixLength);
                route.put("Nexthop", compressIPv6(suffixToNextHop.getOrDefault(suffix,"unknown")));
                route.put("Cost", suffixToCost.getOrDefault(suffix, "0"));
                route.put("ProtoType", suffixToProto.getOrDefault(suffix, "2"));
                route.put("Preference", suffixToPreference.getOrDefault(suffix, "0"));
                route.put("type", getRouteType6(suffixToProto.get(suffix)));
                route.put("Interface", ifIndex);
                route.put("Port", portName);

                routeEntries.add(route);

            } catch (Exception e) {
                System.err.println("路由解析失败: " + destEntry.getKey());
            }
        }

        return routeEntries;
    }

    // 创建后缀索引
    private static void createSuffixIndex(Map<String, String> sourceMap, Map<String, String> indexMap) {
        for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
            String suffix = getOidSuffix(entry.getKey());
            if (suffix != null) {
                indexMap.put(suffix, entry.getValue());
            }
        }
    }



    // 提取OID后缀
    private static String getOidSuffix(String oid) {
        String[] parts = oid.split("\\.");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    // IPv6路由类型转换方法
    private static String getRouteType6(String typeCode) {
        if (typeCode == null || typeCode.equals("N/A")) {
            return "unknown";
        }

        switch (typeCode) {
            case "1": return "Static";
            case "2": return "Direct";
            default: return "unknown(" + typeCode + ")";
        }
    }



    /**
     * 将路由类型代码转换为有意义的名称
     */
    private static String getRouteType(String typeCode) {
        if (typeCode == null || typeCode.equals("N/A")) {
            return "unknown";
        }

        switch (typeCode) {
            case "1":
                return "other";
            case "2":
                return "local";
            case "3":
                return "netmgmt";
            case "4":
                return "icmp";
            case "5":
                return "egp";
            case "6":
                return "ggp";
            case "7":
                return "hello";
            case "8":
                return "rip";
            case "9":
                return "isIs";
            case "10":
                return "esIs";
            case "11":
                return "ciscoIgrp";
            case "12":
                return "bbnSpfIgp";
            case "13":
                return "ospf";
            case "14":
                return "bgp";
            default:
                return "unknown(" + typeCode + ")";
        }
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
            // 入口流量求和
            result.put("in", in);
        }
        if (outData != null && !outData.getVariableBindings().isEmpty()) {
            out = outData.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("\"", "");
            result.put("out", out);
            // 出口流量求和
        }
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


    /**
     * 将冒号分隔的十进制格式转换为标准IPv6地址
     * 特别注意：正确处理全零地址返回"::"
     *
     * @param input 格式如 "252:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0"
     * @return 标准IPv6地址，全零地址返回"::"
     */
    public static String convertToIPv6(String input) {
        // 首先检查是否是全零地址
        if (isAllZeros(input)) {
            return "::";
        }

        // 处理非全零地址
        String[] parts = input.split(":");
        if (parts.length != 16) {
            throw new IllegalArgumentException("输入格式不正确，需要16个用冒号分隔的数字");
        }

        // 构建压缩的IPv6地址
        return buildCompressedIPv6(parts);
    }

    /**
     * 检查输入是否代表全零地址
     */
    private static boolean isAllZeros(String input) {
        String[] parts = input.split(":");
        if (parts.length != 16) return false;

        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value != 0) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 构建压缩的IPv6地址
     */
    private static String buildCompressedIPv6(String[] parts) {
        // 将每部分转换为十六进制
        String[] hexGroups = new String[8];
        for (int i = 0; i < 8; i++) {
            int high = Integer.parseInt(parts[2*i]);
            int low = Integer.parseInt(parts[2*i + 1]);
            hexGroups[i] = String.format("%x%02x", high, low);
        }

        // 压缩连续的零组
        return compressIPv6(hexGroups);
    }

    /**
     * 压缩IPv6地址中的连续零组
     */
    private static String compressIPv6(String[] groups) {
        int maxZeroStart = -1;
        int maxZeroLength = 0;
        int currentZeroStart = -1;
        int currentZeroLength = 0;

        // 查找最长的连续零组
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].equals("0000")) {
                if (currentZeroStart == -1) {
                    currentZeroStart = i;
                    currentZeroLength = 1;
                } else {
                    currentZeroLength++;
                }

                if (currentZeroLength > maxZeroLength) {
                    maxZeroLength = currentZeroLength;
                    maxZeroStart = currentZeroStart;
                }
            } else {
                // 遇到非零组时，重置计数
                currentZeroStart = -1;
                currentZeroLength = 0;
            }
        }

        // 处理结尾的连续零
        if (currentZeroLength > maxZeroLength) {
            maxZeroLength = currentZeroLength;
            maxZeroStart = currentZeroStart;
        }

        // 构建压缩后的地址
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < groups.length; i++) {
            if (maxZeroLength > 1 && i == maxZeroStart) {
                if (i == 0) {
                    result.append("::");
                } else {
                    result.append(":");
                }
                i += maxZeroLength - 1;
                continue;
            }

            // 添加冒号分隔符（除第一个组外）
            if (i > 0 && (i < maxZeroStart || i >= maxZeroStart + maxZeroLength)) {
                result.append(":");
            }

            // 添加组的值（去除前导零）
            String group = groups[i];
            if (!group.equals("0000")) {
                // 去除前导零，但保留至少一个0
                group = group.replaceFirst("^0+", "");
                if (group.isEmpty()) group = "0";
            }
            result.append(group);
        }

        // 处理双冒号特殊情况
        String compressed = result.toString();
        if (compressed.contains("::::")) {
            compressed = compressed.replace("::::", "::");
        }
        if (compressed.contains(":::")) {
            compressed = compressed.replace(":::", "::");
        }
        if (compressed.equals("0")) {
            return "::";
        }

        return compressed;
    }


}
