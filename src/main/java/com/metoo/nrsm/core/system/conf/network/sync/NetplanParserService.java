package com.metoo.nrsm.core.system.conf.network.sync;

import com.metoo.nrsm.entity.Interface;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NetplanParserService {

    public List<Interface> parseNetplanConfig(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> netplanConfig = yaml.load(inputStream);
        return parseConfig(netplanConfig);
    }

    private List<Interface> parseConfig(Map<String, Object> netplanConfig) {
        Map<String, Object> network = (Map<String, Object>) netplanConfig.get("network");
        Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
        Map<String, Object> bridges = (Map<String, Object>) network.get("bridges");

        List<Interface> interfaces = new ArrayList<>();
        Map<String, Interface> interfaceMap = new HashMap<>();

        // 1. 先解析所有主接口
        for (Map.Entry<String, Object> entry : ethernets.entrySet()) {
            Interface intf = createInterfaceFromConfig(entry.getKey(),
                    (Map<String, Object>) entry.getValue(),
                    null);
            try {
                // 获取网络主接口的实时状态
                NetworkInterface netIntf = NetworkInterface.getByName(intf.getName());
                if (netIntf != null) {
                    boolean isUp = netIntf.isUp(); // 接口是否启用
                    intf.setIsup(isUp ? "up" : "dowm"); // 假设 Interface 类有 setIsUp 方法
                } else {
                    intf.setIsup("dowm");
                }
                interfaceMap.put(intf.getName(), intf);
                interfaces.add(intf);
            } catch (SocketException e) {
                e.printStackTrace();
            }

        }

        // 2. 解析网桥接口 (处理方式与以太网接口一致)
        if (bridges != null) {
            for (Map.Entry<String, Object> entry : bridges.entrySet()) {
                String bridgeName = entry.getKey();
                Map<String, Object> config = (Map<String, Object>) entry.getValue();

                // 创建网桥接口
                Interface bridgeIntf = createInterfaceFromConfig(bridgeName, config);
                updateInterfaceStatus(bridgeIntf);
                interfaces.add(bridgeIntf);

                // 处理成员接口
                handleBridgeMembers(config, bridgeName, interfaces, interfaceMap);
            }
        }

        // 2. 解析VLAN接口(此时可以引用已解析的主接口)
        // 仅解析主接口，不解析vlan接口
//        if (network.containsKey("vlans")) {
//            Map<String, Object> vlans = (Map<String, Object>) network.get("vlans");
//            for (Map.Entry<String, Object> entry : vlans.entrySet()) {
//                Map<String, Object> vlanConfig = (Map<String, Object>) entry.getValue();
//                String parentName = (String) vlanConfig.get("link");
//                Interface parent = interfaceMap.get(parentName);
//
//                Interface intf = createInterfaceFromConfig(entry.getKey(), vlanConfig,
//                        parent != null ? parent.getId() : null);
//                intf.setVlanNum((Integer) vlanConfig.get("id"));
//                interfaces.add(intf);
//            }
//        }

        return interfaces;
    }

    private void handleBridgeMembers(Map<String, Object> bridgeConfig,
                                     String bridgeName,
                                     List<Interface> interfaces,
                                     Map<String, Interface> interfaceMap) {
        if (bridgeConfig.containsKey("interfaces")) {
            List<String> memberNames = (List<String>) bridgeConfig.get("interfaces");
            for (String memberName : memberNames) {
                Interface member = interfaceMap.get(memberName);

                // 创建新的成员接口（如果尚未解析）
                if (member == null) {
                    member = new Interface();
                    member.setName(memberName);
                    member.setParentName(bridgeName);
                    updateInterfaceStatus(member);
                    interfaces.add(member);
                    interfaceMap.put(memberName, member);
                }
                // 更新已有成员接口
                else {
                    member.setParentName(bridgeName);
                }
            }
        }
    }

    private void updateInterfaceStatus(Interface intf) {
        try {
            NetworkInterface netIntf = NetworkInterface.getByName(intf.getName());
            if (netIntf != null) {
                intf.setIsup(netIntf.isUp() ? "up" : "down");
            } else {
                intf.setIsup("down");
            }
        } catch (SocketException e) {
            intf.setIsup("down");
        }
    }

    private Interface createInterfaceFromConfig(String name, Map<String, Object> config) {
        Interface intf = new Interface();
        intf.setName(name);

        // 解析IP地址 (保留CIDR格式)
        if (config.containsKey("addresses")) {
            List<String> addresses = (List<String>) config.get("addresses");
            for (String addr : addresses) {
                if (addr.contains(".")) {
                    intf.setIpv4Address(addr);
                } else if (addr.contains(":")) {
                    intf.setIpv6Address(addr);
                }
            }
        }

        // 解析网关
        if (config.containsKey("gateway4")) {
            intf.setGateway4((String) config.get("gateway4"));
        }
        if (config.containsKey("gateway6")) {
            intf.setGateway6((String) config.get("gateway6"));
        }

        return intf;
    }


    private Interface createInterfaceFromConfig(String name,
                                                Map<String, Object> config,
                                                Long parentId) {
        Interface intf = new Interface();
        intf.setName(name);
        intf.setParentId(parentId);

        // 处理IP地址
        if (config.containsKey("addresses")) {
            List<String> addresses = (List<String>) config.get("addresses");
            for (String addr : addresses) {
                if (addr.contains(".")) { // IPv4
//                    intf.setIpv4address(addr.split("/")[0]);
                    intf.setIpv4Address(addr);
                } else if (addr.contains(":")) { // IPv6
//                    intf.setIpv6address(addr.split("/")[0]);
                    intf.setIpv6Address(addr);
                }
            }
        }

        // 处理网关
        if (config.containsKey("gateway4")) {
            intf.setGateway4((String) config.get("gateway4"));
        } else if (config.containsKey("routes")) {
            List<Map<String, String>> routes = (List<Map<String, String>>) config.get("routes");
            for (Map<String, String> route : routes) {
                if ("0.0.0.0/0".equals(route.get("to"))) {
                    intf.setGateway4(route.get("via"));
                    break;
                }
            }
        }

        return intf;
    }
}