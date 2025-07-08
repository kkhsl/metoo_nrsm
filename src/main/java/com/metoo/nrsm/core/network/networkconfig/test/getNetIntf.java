package com.metoo.nrsm.core.network.networkconfig.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class getNetIntf {

    public static void main(String[] args) throws Exception {
        String networkInterfaces = getNetworkInterfaces();
        System.out.println(networkInterfaces);
    }

    /**
     * getnetintf.py
     *
     * @return
     * @throws Exception
     */
    public static String getNetworkInterfaces() throws Exception {
        // 空列表初始化
        Map<String, Object> yamlData = new Yaml().load(
                new FileInputStream("/etc/netplan/00-installer-config.yaml")
        );
//        Map<String, Object> yamlData = new Yaml().load(
//                new FileInputStream("C:\\Users\\leo\\Desktop\\00-installer-config.yaml")
//        );

        Map<String, Object> network = safeCast(yamlData.get("network"), Map.class);
        Map<String, Object> ethernets = safeCast(network.get("ethernets"), Map.class);

        Map<String, Boolean> interfaceStatus = new HashMap<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            interfaceStatus.put(ni.getName(), ni.isUp());
        }

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String iface : ethernets.keySet()) {
            Map<String, Object> ifaceConfig = safeCast(ethernets.get(iface), Map.class);
            // 传入接口名称
            Map<String, Object> info = processInterface(iface, // 添加接口名称参数
                    ifaceConfig,
                    interfaceStatus.getOrDefault(iface, false)
            );
            result.put(iface, info);
        }

        return new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(sortInterfaces(result));
    }

    private static void processNameservers(Map<String, Object> config, Map<String, Object> info) {
        if (config.containsKey("nameservers")) {
            Map<String, Object> nameservers = safeCast(config.get("nameservers"), Map.class);
            info.put("nameservers", new HashMap<String, Object>() {{
                put("addresses", nameservers.getOrDefault("addresses", Collections.emptyList()));
                put("search", nameservers.getOrDefault("search", Collections.emptyList()));
            }});
        } else {
            info.put("nameservers", new HashMap<String, Object>() {{
                put("addresses", Collections.emptyList());
                put("search", Collections.emptyList());
            }});
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T safeCast(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }
        throw new IllegalArgumentException("Type mismatch for: " + obj);
    }

    private static Map<String, Object> processInterface(String ifaceName, Map<String, Object> config, boolean isUp) {
        Map<String, Object> info = new LinkedHashMap<>();

        // 修正后的状态检测逻辑
        try {
            NetworkInterface ni = NetworkInterface.getByName(ifaceName);
            isUp = ni != null && ni.isUp();
        } catch (SocketException e) {
            isUp = false;
        }

        // 处理地址信息
        List<String> addresses = config.containsKey("addresses") ?
                safeCast(config.get("addresses"), List.class) :
                Collections.emptyList();

        info.put("ipv4address", findAddress(addresses, false));
        info.put("ipv6address", findAddress(addresses, true));

        // 处理网关
        info.put("gateway4", config.getOrDefault("gateway4", ""));
        info.put("gateway6", config.getOrDefault("gateway6", ""));

        // 新增DNS处理
        processNameservers(config, info);

        // 更新状态
        info.put("isup", isUp ? "up" : "down");

        return info;
    }

    private static String findAddress(List<String> addresses, boolean ipv6) {
        for (String addr : addresses) {
            if (ipv6 == addr.contains(":")) {
                return addr;
            }
        }
        return "";
    }

    // 参数类型为 Map<String, Map<String, Object>>
    private static Map<String, Map<String, Object>> sortInterfaces(
            Map<String, Map<String, Object>> data
    ) {
        LinkedHashMap<String, Map<String, Object>> ordered = new LinkedHashMap<>();
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(e -> ordered.put(e.getKey(), e.getValue()));
        return ordered;
    }


}