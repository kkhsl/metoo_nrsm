package com.metoo.nrsm.core.config.utils.gather.factory.gather.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

/**
 * hk
 */
public class TrafficAnalyzerUtils {

    // 配置静态 ObjectMapper 实例，支持单引号
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true); // 保持属性名原样;



    // 常量定义
    private static final String VLAN_KEY = "Vlan";
    private static final String DIRECTION_KEY = "Direction";
    private static final String IPV4_BYTES_KEY = "IPv4Bytes";
    private static final String IPV6_BYTES_KEY = "IPv6Bytes";

    public static void main(String[] args) {
        String jsonData = "[{'Vlan': '100', 'Direction': 'Inbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '10497595529', 'IPv6Bytes': '0'}, " +
                "{'Vlan': '100', 'Direction': 'Outbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '6716463687', 'IPv6Bytes': '0'}, " +
                "{'Vlan': '200', 'Direction': 'Inbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '32583387286', 'IPv6Bytes': '428540419'}, " +
                "{'Vlan': '200', 'Direction': 'Outbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '161483751704', 'IPv6Bytes': '0'}, " +
                "{'Vlan': '300', 'Direction': 'Inbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '314038407', 'IPv6Bytes': '8464052'}, " +
                "{'Vlan': '300', 'Direction': 'Outbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '861964134', 'IPv6Bytes': '0'}]";

        try {
            String trafficStats = analyzer(jsonData);
//            printStats(trafficStats);
//            System.out.println("\nJSON格式输出:");
//            printStatsAsJson(trafficStats);
            System.out.println(trafficStats);
        } catch (IllegalArgumentException e) {
            System.err.println("分析失败: " + e.getMessage());
        }
    }

    public static String analyzer(String jsonData) throws IllegalArgumentException {
        try {
            // 参数校验
            if (jsonData == null || jsonData.trim().isEmpty()) {
                throw new IllegalArgumentException("输入数据不能为空");
            }

            // JSON解析
            List<Map<String, Object>> rawData = MAPPER.readValue(
                    jsonData,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 处理数据
            List<TrafficStats> trafficStats = processRawData(rawData);

            return convertToString(trafficStats);
        } catch (Exception e) {
            throw new IllegalArgumentException("数据处理错误: " + e.getMessage(), e);
        }
    }

    private static List<TrafficStats> processRawData(List<Map<String, Object>> rawData) {
        Map<String, TrafficStats> statsMap = new HashMap<>();

        for (Map<String, Object> entry : rawData) {
            try {
                processSingleEntry(entry, statsMap);
            } catch (Exception e) {
                System.err.println("跳过无效记录: " + entry + ", 错误: " + e.getMessage());
            }
        }

        return new ArrayList<>(statsMap.values());
    }

    private static void processSingleEntry(Map<String, Object> entry, Map<String, TrafficStats> statsMap) {
        String vlan = getStringValue(entry, VLAN_KEY);
        if (vlan == null || vlan.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少VLAN标识");
        }

        long ipv4Bytes = getLongValue(entry, IPV4_BYTES_KEY);
        long ipv6Bytes = getLongValue(entry, IPV6_BYTES_KEY);

        TrafficStats stats = statsMap.computeIfAbsent(
                vlan,
                TrafficStats::new
        );

        stats.addIPv4(ipv4Bytes);
        stats.addIPv6(ipv6Bytes);
    }

    // 辅助方法
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private static long getLongValue(Map<String, Object> map, String key) {
        String strValue = getStringValue(map, key);
        if (strValue == null || strValue.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            System.err.println("数值格式错误: " + strValue);
            return 0L;
        }
    }

    // 输出方法
//    private static void printStats(List<TrafficStats> stats) {
//        System.out.println("VLAN\tIPv4流量(MB)\tIPv6流量(MB)");
//        stats.stream()
//                .sorted(Comparator.comparing(TrafficStats::getVlan))
//                .forEach(stat -> System.out.printf("%s\t%.2f\t\t%.2f%n",
//                        stat.getVlan(), stat.getIPv4MB(), stat.getIPv6MB()));
//    }
//
//    private static void printStatsAsJson(List<TrafficStats> stats) {
//        String json = stats.stream()
//                .sorted(Comparator.comparing(TrafficStats::getVlan))
//                .map(stat -> String.format(
//                        "  {\"Vlan\": \"%s\", \"IPv4_MB\": %.2f, \"IPv6_MB\": %.2f}",
//                        stat.getVlan(), stat.getIPv4MB(), stat.getIPv6MB()))
//                .collect(Collectors.joining(",\n", "[\n", "\n]"));
//
//        System.out.println(json);
//    }

    private static final double BYTES_TO_MB = 1_000_000.0;  // 字节到MB的转换系数

    // Jackson 库默认情况下是通过 getter 方法来序列化对象
    // 内部类
    static class TrafficStats {
        @JsonProperty("Vlan")  // 保持原样或自定义名称
        private final String Vlan;
        private long ipv4Bytes = 0;
        private long ipv6Bytes = 0;

        private double IPv4MB = 0;
        private double IPv6MB = 0;

        public TrafficStats(String Vlan) {
            this.Vlan = Vlan;
        }

        public String getVlan() {
            return Vlan;
        }

        public void addIPv4(long bytes) {
            this.ipv4Bytes += bytes;
        }

        public void addIPv6(long bytes) {
            this.ipv6Bytes += bytes;
        }

        public double getIPv4MB() {
            return ipv4Bytes / BYTES_TO_MB;
        }

        public double getIPv6MB() {
            return ipv6Bytes / BYTES_TO_MB;
        }

    }

    /**
     * 将 TrafficStats 列表转换为 JSON 字符串
     * @param statsList TrafficStats 列表
     * @return JSON 格式字符串
     */
    public static String convertToString(List<TrafficStats> statsList) {
        try {
            return MAPPER.writeValueAsString(statsList);
        } catch (Exception e) {
            throw new IllegalArgumentException("转换为字符串失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 JSON 字符串解析为 TrafficStats 列表
     * @param jsonString JSON 格式字符串
     * @return TrafficStats 列表
     */
    public static List<TrafficStats> parseFromString(String jsonString) {
        try {
            return MAPPER.readValue(jsonString,
                    new TypeReference<List<TrafficStats>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("字符串解析失败: " + e.getMessage(), e);
        }
    }

//  新增方法：计算流量差值
    public static String calculateFlowDifference(String newDataJson, String oldDataJson) {
        try {
            // 1. 解析新旧JSON数据
            List<Map<String, Object>> newData = parseJsonData(newDataJson);
            List<Map<String, Object>> oldData = oldDataJson == null || oldDataJson.trim().isEmpty()
                    ? Collections.emptyList()
                    : parseJsonData(oldDataJson);

            // 2. 如果oldData为空，直接返回newData
            if (oldData == null || oldData.isEmpty()) {
                return MAPPER.writeValueAsString(newData);
            }

            // 3. 转换为按VLAN分组的Map便于查找
            Map<String, Map<String, Object>> oldDataMap = oldData.stream()
                    .collect(Collectors.toMap(
                            item -> item.get("Vlan").toString(),
                            item -> item
                    ));

            // 4. 计算差值
            List<Map<String, Object>> result = newData.stream()
                    .map(newItem -> {
                        Map<String, Object> diffItem = new HashMap<>();
                        String vlan = newItem.get("Vlan").toString();
                        diffItem.put("Vlan", vlan);

                        // 获取对应的旧数据
                        Map<String, Object> oldItem = oldDataMap.getOrDefault(vlan, createEmptyItem(vlan));

                        // 计算IPv4差值
                        double newIpv4 = Double.parseDouble(newItem.get("ipv4MB").toString());
                        double oldIpv4 = Double.parseDouble(oldItem.get("ipv4MB").toString());
                        diffItem.put("ipv4MB", newIpv4 - oldIpv4);

                        // 计算IPv6差值
                        double newIpv6 = Double.parseDouble(newItem.get("ipv6MB").toString());
                        double oldIpv6 = Double.parseDouble(oldItem.get("ipv6MB").toString());
                        diffItem.put("ipv6MB", newIpv6 - oldIpv6);

                        return diffItem;
                    })
                    .collect(Collectors.toList());

            // 5. 转换回JSON字符串
            return MAPPER.writeValueAsString(result);

        } catch (Exception e) {
            throw new RuntimeException("计算流量差值失败: " + e.getMessage(), e);
        }
    }

    private static List<Map<String, Object>> parseJsonData(String json) throws Exception {
        return MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    }

    private static Map<String, Object> createEmptyItem(String vlan) {
        Map<String, Object> item = new HashMap<>();
        item.put("Vlan", vlan);
        item.put("ipv4MB", 0.0);
        item.put("ipv6MB", 0.0);
        return item;
    }
}
