package com.metoo.nrsm.core.network.networkconfig.test;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class modifyVlans {

    private static final String NETPLAN_PATH = "/etc/netplan/00-installer-config.yaml";
//    private static final String NETPLAN_PATH = "C:\\Users\\leo\\Desktop\\00-installer-config.yaml";


    /**
     * 修改VLAN配置
     *
     * @param parentInterface 父接口名称 (如 enp2s0f1)
     * @param vlanId          VLAN ID
     * @param ipv4Address     IPv4地址（含掩码），空表示删除
     * @param ipv6Address     IPv6地址（含掩码），空表示删除
     * @param gateway4        IPv4网关，空表示删除
     * @param gateway6        IPv6网关，空表示删除
     * @return 操作结果状态码
     */
    public static int modifyVlanConfig(String parentInterface, String vlanId,
                                       String ipv4Address, String ipv6Address,
                                       String gateway4, String gateway6) throws Exception {
        // 加载配置
        Yaml yaml = new Yaml();
        Map<String, Object> originalConfig;
        try (InputStream input = Files.newInputStream(Paths.get(NETPLAN_PATH))) {
            originalConfig = yaml.load(input);
        }
        Map<String, Object> backupConfig = deepCopy(originalConfig);

        try {
            Map<String, Object> network = (Map<String, Object>) originalConfig.get("network");
            // 更新父接口的ethernets配置
            Map<String, Object> ethernets = (Map<String, Object>) network.computeIfAbsent("ethernets", k -> new LinkedHashMap<>());
            Map<String, Object> newParentConfig = new LinkedHashMap<>();
            newParentConfig.put("dhcp4", false);
            newParentConfig.put("dhcp6", false);
            ethernets.put(parentInterface, newParentConfig);

            Map<String, Object> vlans = (Map<String, Object>) network.computeIfAbsent("vlans", k -> new LinkedHashMap<>());
            String vlanKey = parentInterface + "." + vlanId;

            // 判断是否为全空删除操作
            boolean isDeleteOperation = ipv4Address.isEmpty() && ipv6Address.isEmpty()
                    && gateway4.isEmpty() && gateway6.isEmpty();

            if (isDeleteOperation) {
                // 完全删除VLAN配置
                vlans.remove(vlanKey);

                // 如果vlans为空则移除整个vlans节点
                if (vlans.isEmpty()) {
                    network.remove("vlans");
                }
            } else {
                // 创建/更新配置（原有逻辑）
                Map<String, Object> newConfig = new LinkedHashMap<>();
                newConfig.put("id", Integer.parseInt(vlanId));
                newConfig.put("link", parentInterface);

                // 地址处理
                List<String> addresses = new ArrayList<>();
                if (!ipv4Address.isEmpty()) addresses.add(ipv4Address);
                if (!ipv6Address.isEmpty()) addresses.add(ipv6Address);
                if (!addresses.isEmpty()) newConfig.put("addresses", addresses);

                // 路由处理
                List<Map<String, String>> routes = new ArrayList<>();
                if (!gateway4.isEmpty()) routes.add(createRoute("0.0.0.0/0", gateway4));
                if (!gateway6.isEmpty()) routes.add(createRoute("::/0", gateway6));
                if (!routes.isEmpty()) newConfig.put("routes", routes);

                vlans.put(vlanKey, newConfig);
            }

            // 写入并应用配置
            writeYamlConfig(originalConfig);
            return executeNetplanApply();
        } catch (Exception e) {
            restoreConfig(backupConfig);
            throw new IOException("配置操作失败: " + e.getMessage(), e);
        }
    }

    // 辅助方法：创建路由条目
    private static Map<String, String> createRoute(String to, String via) {
        Map<String, String> route = new LinkedHashMap<>();
        route.put("to", to);
        route.put("via", via);
        return route;
    }

    private static List<Map<String, String>> buildRoutes(String gw4, String gw6) {
        List<Map<String, String>> routes = new ArrayList<>();
        if (gw4 != null && !gw4.isEmpty()) {
            routes.add(new LinkedHashMap<String, String>() {{
                put("to", "0.0.0.0/0");
                put("via", gw4);
            }});
        }
        if (gw6 != null && !gw6.isEmpty()) {
            routes.add(new LinkedHashMap<String, String>() {{
                put("to", "::/0");
                put("via", gw6);
            }});
        }
        return routes;
    }

    private static void writeYamlConfig(Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setExplicitStart(false);

        Representer representer = new Representer(options) {
            @Override
            protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
                Map<Object, Object> filtered = new LinkedHashMap<>();
                mapping.forEach((k, v) -> {
                    // 保留所有显式配置的键值对
                    if (v instanceof Map) {
                        Map<?, ?> sub = filterEmptyMaps((Map<?, ?>) v);
                        if (!sub.isEmpty()) filtered.put(k, sub);
                    } else if (v != null) {
                        filtered.put(k, v);
                    }
                });
                return super.representMapping(tag, filtered, flowStyle);
            }

            private Map<?, ?> filterEmptyMaps(Map<?, ?> map) {
                Map<Object, Object> result = new LinkedHashMap<>();
                map.forEach((k, v) -> {
                    if (v instanceof Map) {
                        Map<?, ?> sub = filterEmptyMaps((Map<?, ?>) v);
                        if (!sub.isEmpty()) result.put(k, sub);
                    } else if (v != null) {
                        result.put(k, v);
                    }
                });
                return result;
            }
        };

        // 确保布尔值false会被输出
        representer.addClassTag(LinkedHashMap.class, Tag.MAP);
        Yaml yaml = new Yaml(representer, options);

        try (Writer writer = Files.newBufferedWriter(Paths.get(NETPLAN_PATH))) {
            yaml.dump(config, writer);
        }
    }


    private static int executeNetplanApply() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("netplan", "apply");
        Process p = pb.start();
        return p.waitFor();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<String, Object> original) {
        Yaml yaml = new Yaml();
        return yaml.load(yaml.dump(original));
    }

    private static void restoreConfig(Map<String, Object> backup) throws IOException {
        writeYamlConfig(backup);
    }

    public static void main(String[] args) {
        try {
            // 添加/更新 VLAN 200（全参数）
            int result = modifyVlanConfig("enp2s0f2", "900",
                    "192.168.6.102/25", "fc00:1000:0:1::3/65",
                    "192.168.6.1", "fc00:1000:0:1::1");

            // 更新部分参数
//            int result = modifyVlanConfig("enp2s0f1", "500",
//                    "192.168.6.102/24", "", "192.168.6.1", "");

            // 删除配置（清空所有参数）
//            int result = modifyVlanConfig("enp2s0f1", "500", "", "", "", "");

            System.out.println("操作结果: " + (result == 0 ? "成功" : "失败"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}