package com.metoo.nrsm.core.network.networkconfig.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.*;

public class getNetntf {

    /**
     * getnetintf.py
     * @return  Exception
     * @throws Exception
     */
    public static String getNetworkInterfaces() throws Exception {
        // 1. 读取并解析netplan YAML配置
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData;
        try (FileReader reader = new FileReader("/etc/netplan/00-installer-config.yaml")) {
            yamlData = yaml.load(reader);
        }

        // 提取ethernets部分
        Map<String, Map<String, Object>> ethernets = ((Map<String, Map<String, Map<String, Object>>>) yamlData.get("network")).get("ethernets");
        Map<String, Map<String, Object>> processedData = new HashMap<>();

        // 2. 获取物理接口状态
        Map<String, Boolean> interfaceStatus = new HashMap<>();
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (interfaceStatus.put(networkInterface.getName(), networkInterface.isUp()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        // 3. 处理每个接口配置
        for (String iface : ethernets.keySet()) {
            Map<String, Object> ifaceConfig = ethernets.get(iface);
            Map<String, Object> info = new HashMap<>();

            // 处理地址和网关
            processAddresses(ifaceConfig, info);
            processGateways(ifaceConfig, info);

            // 设置接口状态
            info.put("isup", interfaceStatus.getOrDefault(iface, false) ? "up" : "down");

            processedData.put(iface, info);
        }

        // 4. 接口重命名和排序
        Map<String, Map<String, Object>> renamedData = renameInterfaces(processedData);
        LinkedHashMap<String, Map<String, Object>> orderedData = orderInterfaces(renamedData);

        // 5. 转换为JSON并返回
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(orderedData);
    }

    private static void processAddresses(Map<String, Object> config, Map<String, Object> info) {
        List<String> addresses = (List<String>) config.get("addresses");
        info.put("ipv4address", "");
        info.put("ipv6address", "");

        if (addresses != null && !addresses.isEmpty()) {
            for (String addr : addresses) {
                String ip = addr.split("/")[0];
                if (isIPv6(ip)) {
                    info.put("ipv6address", addr);
                } else {
                    info.put("ipv4address", addr);
                }
            }
        }
    }

    private static void processGateways(Map<String, Object> config, Map<String, Object> info) {
        info.put("gateway4", config.getOrDefault("gateway4", ""));
        info.put("gateway6", config.getOrDefault("gateway6", ""));
    }

    private static boolean isIPv6(String address) {
        return address.contains(":");
    }

    private static Map<String, Map<String, Object>> renameInterfaces(Map<String, Map<String, Object>> data) {
        Map<String, Map<String, Object>> renamed = new HashMap<>(data);
        renamed.put("xge0", renamed.remove("eth0"));
        renamed.put("xge1", renamed.remove("eth1"));
        renamed.put("mgt0", renamed.remove("eth2"));
        return renamed;
    }

    private static LinkedHashMap<String, Map<String, Object>> orderInterfaces(Map<String, Map<String, Object>> data) {
        LinkedHashMap<String, Map<String, Object>> ordered = new LinkedHashMap<>();
        String[] order = {"ge0", "ge1", "ge2", "ge3", "ge4", "ge5", "ge6", "ge7", "xge0", "xge1", "mgt0"};
        for (String key : order) {
            if (data.containsKey(key)) {
                ordered.put(key, data.get(key));
            }
        }
        return ordered;
    }

    /*public static void main(String[] args) throws Exception {
        Session session = null;
        ChannelExec channel = null;
        Map<String, String> status = new HashMap<>();
        try {
            session = SnmpHelper.createSession();
            channel = (ChannelExec) session.openChannel("exec");
            // 2. 执行两个远程命令
            String yamlContent = executeRemoteCommand(channel, "cat /etc/netplan/00-installer-config.yaml");
            String interfaceStatus = executeRemoteCommand(channel, "ip -o link show | awk '{print $2,$9}'");

            // 3. 解析接口状态
            Map<String, Boolean> statusMap = parseInterfaceStatus(interfaceStatus);

            // 4. 解析YAML配置
            Map<String, Map<String, Object>> processedData = parseYamlConfig(yamlContent, statusMap);

            // 5. 生成最终JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            System.out.println(mapper.writeValueAsString(processedData));
        } catch (Exception e) {
            System.err.println("Error during remote connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private static String executeRemoteCommand(ChannelExec channel, String command) throws Exception {
        channel.setCommand(command);
        channel.setInputStream(null);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        channel.setOutputStream(outputBuffer);
        channel.connect();

        // 等待命令执行完成
        while (!channel.isClosed()) {
            Thread.sleep(500);
        }

        return outputBuffer.toString();
    }

    private static Map<String, Boolean> parseInterfaceStatus(String rawOutput) {
        Map<String, Boolean> statusMap = new HashMap<>();
        for (String line : rawOutput.split("\n")) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                String iface = parts[0].replace(":", "");
                boolean isUp = parts[1].equalsIgnoreCase("UP");
                statusMap.put(iface, isUp);
            }
        }
        return statusMap;
    }

    private static Map<String, Map<String, Object>> parseYamlConfig(String yamlContent,
                                                                    Map<String, Boolean> statusMap) {
        // YAML解析
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(yamlContent);

        // 获取ethernets配置
        Map<String, Map<String, Object>> ethernets = getNestedMap(yamlData, "network", "ethernets");

        // 处理每个接口
        Map<String, Map<String, Object>> processed = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : ethernets.entrySet()) {
            String iface = entry.getKey();
            Map<String, Object> config = entry.getValue();
            processed.put(iface, processInterface(config, statusMap.getOrDefault(iface, false)));
        }

        // 接口重命名和排序
        return orderInterfaces2(renameInterfaces1(processed));
    }

    private static Map<String, Object> processInterface(Map<String, Object> config, boolean isUp) {
        Map<String, Object> info = new HashMap<>();

        // 处理地址
        List<String> addresses = (List<String>) config.get("addresses");
        addresses = addresses != null ? addresses : Collections.emptyList();
        for (String addr : addresses) {
            String ip = addr.split("/")[0];
            if (ip.contains(":")) {
                info.put("ipv6address", addr);
            } else {
                info.put("ipv4address", addr);
            }
        }

        // 处理网关
        info.put("gateway4", config.getOrDefault("gateway4", ""));
        info.put("gateway6", config.getOrDefault("gateway6", ""));
        info.put("isup", isUp ? "up" : "down");

        return info;
    }

    private static Map<String, Map<String, Object>> renameInterfaces1(Map<String, Map<String, Object>> data) {
        Map<String, Map<String, Object>> renamed = new HashMap<>(data);
        renameEntry(renamed, "eth0", "xge0");
        renameEntry(renamed, "eth1", "xge1");
        renameEntry(renamed, "eth2", "mgt0");
        return renamed;
    }

    private static void renameEntry(Map<String, Map<String, Object>> map,
                                    String oldKey, String newKey) {
        if (map.containsKey(oldKey)) {
            map.put(newKey, map.remove(oldKey));
        }
    }

    private static Map<String, Map<String, Object>> orderInterfaces2(Map<String, Map<String, Object>> data) {
        LinkedHashMap<String, Map<String, Object>> ordered = new LinkedHashMap<>();
        String[] order = {"ge0","ge1","ge2","ge3","ge4","ge5","ge6","ge7","xge0","xge1","mgt0"};
        for (String key : order) {
            if (data.containsKey(key)) ordered.put(key, data.get(key));
        }
        return ordered;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> getNestedMap(Map<String, Object> data, String... keys) {
        Map<String, Object> current = data;

        try {
            for (String key : keys) {
                Object value = current.get(key);

                // 类型安全检查
                if (!(value instanceof Map)) {
                    System.err.println("警告: 键 '" + key + "' 的值不是Map类型，实际类型为: "
                            + (value != null ? value.getClass().getSimpleName() : "null"));
                    return Collections.emptyMap();
                }

                current = (Map<String, Object>) value;
            }

            // 最终结果类型验证
            Map<String, Map<String, Object>> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : current.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    result.put(entry.getKey(), (Map<String, Object>) entry.getValue());
                } else {
                    System.err.println("警告: 接口 '" + entry.getKey() + "' 的配置不是Map类型");
                }
            }
            return result;

        } catch (Exception e) {
            System.err.println("解析YAML结构失败: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
*/
}