package com.metoo.nrsm.core.network.networkconfig.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class getAllNetIntf {

    public static void main(String[] args) throws Exception {
        String networkInterfaces = getNetworkInterfaces();
        System.out.println(networkInterfaces);
    }

    public static String getNetworkInterfaces() throws Exception {

        Map<String, Object> yamlData = new Yaml().load(
                new FileInputStream("/etc/netplan/00-installer-config.yaml")
        );

//        Map<String, Object> yamlData = new Yaml().load(
//                new FileInputStream("C:\\Users\\leo\\Desktop\\00-installer-config.yaml")
//        );

        Map<String, Object> network = safeCast(yamlData.get("network"), Map.class);
        Map<String, Object> ethernets = safeCast(network.get("ethernets"), Map.class);
        Map<String, Object> vlans = safeCast(network.getOrDefault("vlans", Collections.emptyMap()), Map.class);

        // 处理VLAN配置并按link分组
        Map<String, List<Map<String, Object>>> vlanGroups = new HashMap<>();
        for (String vlanKey : vlans.keySet()) {
            Map<String, Object> vlanConfig = safeCast(vlans.get(vlanKey), Map.class);
            String link = (String) vlanConfig.get("link");
            List<Map<String, Object>> list = vlanGroups.computeIfAbsent(link, k -> new ArrayList<>());
            list.add(processVlan(vlanConfig));
        }

        // 获取接口状态
        Map<String, Boolean> interfaceStatus = new HashMap<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            interfaceStatus.put(ni.getName(), ni.isUp());
        }

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String iface : ethernets.keySet()) {
            Map<String, Object> ifaceConfig = safeCast(ethernets.get(iface), Map.class);
            boolean isUp = interfaceStatus.getOrDefault(iface, false);

            // 处理基础接口信息
            Map<String, Object> info = processInterface(iface, ifaceConfig, isUp);

            // 添加VLAN信息
            List<Map<String, Object>> vlanList = vlanGroups.getOrDefault(iface, Collections.emptyList());
            info.put("vlans", vlanList);

            result.put(iface, info);
        }

        return new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(sortInterfaces(result));
    }

    private static Map<String, Object> processVlan(Map<String, Object> vlanConfig) {
        Map<String, Object> vlanInfo = new LinkedHashMap<>();

        // 提取VLAN ID
        String id = vlanConfig.get("id").toString();
        vlanInfo.put("id", id);

        // 获取父接口并生成VLAN接口名
        String link = (String) vlanConfig.get("link");
        String vlanInterfaceName = link + "." + id;

        // 获取接口状态
        boolean isUp = false;
        try {
            NetworkInterface ni = NetworkInterface.getByName(vlanInterfaceName);
            isUp = ni != null && ni.isUp();
        } catch (SocketException e) {
            // 接口不存在或无法访问
            isUp = false;
        }
        vlanInfo.put("isup", isUp ? "up" : "down");

        // 处理地址
        List<String> addresses = vlanConfig.containsKey("addresses") ?
                safeCast(vlanConfig.get("addresses"), List.class) : Collections.emptyList();
        vlanInfo.put("ipv4address", findAddress(addresses, false));
        vlanInfo.put("ipv6address", findAddress(addresses, true));

        // 处理路由获取网关
        List<Map<String, Object>> routes = vlanConfig.containsKey("routes") ?
                safeCast(vlanConfig.get("routes"), List.class) : Collections.emptyList();
        String gateway4 = "";
        String gateway6 = "";
        for (Map<String, Object> route : routes) {
            String to = (String) route.get("to");
            String via = (String) route.get("via");
            if ("0.0.0.0/0".equals(to)) gateway4 = via;
            if ("::/0".equals(to)) gateway6 = via;
        }
        vlanInfo.put("gateway4", gateway4);
        vlanInfo.put("gateway6", gateway6);

        return vlanInfo;
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