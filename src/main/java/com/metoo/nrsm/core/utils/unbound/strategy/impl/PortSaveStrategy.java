package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;
import com.metoo.nrsm.entity.Interface;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PortSaveStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        List<Interface> interfaces = (List<Interface>) configData;

        // 1. 分类处理接口
        // a. 有效 IPv4 接口
        List<String> ipv4Interfaces = interfaces.stream()
                .filter(intf -> isValidIPv4(intf.getIpv4address()))
                .map(intf -> String.format("        interface: %s  # %s",
                        extractPureIp(intf.getIpv4address()), intf.getName()))
                .collect(Collectors.toList());

        // b. 有效 IPv6 接口
        List<String> ipv6Interfaces = interfaces.stream()
                .filter(intf -> isValidIPv6(intf.getIpv6address()))
                .map(intf -> String.format("        interface: %s  # %s",
                        extractPureIp(intf.getIpv6address()), intf.getName()))
                .collect(Collectors.toList());

        // c. 空 IP 的接口名称集合
        List<String> emptyInterfaceNames = interfaces.stream()
                .filter(intf ->
                        !isValidIPv4(intf.getIpv4address()) &&
                                !isValidIPv6(intf.getIpv6address()))
                .map(Interface::getName)
                .collect(Collectors.toList());

        // 2. 判断是否需要启用 IPv6
        boolean hasIPv6 = !ipv6Interfaces.isEmpty();

        // 3. 重构配置文件
        List<String> newLines = new ArrayList<>();
        boolean inServerBlock = false;
        boolean hasProcessedInterfaces = false;

        for (String line : lines) {
            if (line.trim().startsWith("server:")) {
                inServerBlock = true;
                newLines.add(line);
                continue;
            }

            if (inServerBlock) {
                // 3.1 跳过所有旧接口行
                if (line.trim().startsWith("interface:")) {
                    continue;
                }

                // 3.2 处理 do-ip6 行
                if (line.trim().startsWith("do-ip6:")) {
                    // 生成带注释的 do-ip6 行
                    String comment = !emptyInterfaceNames.isEmpty() ?
                            "  # " + String.join(", ", emptyInterfaceNames) : "";
                    String newDoIp6Line = String.format("        do-ip6: %s%s",
                            hasIPv6 ? "yes" : "no", comment);

                    // 插入新配置
                    newLines.addAll(ipv4Interfaces);  // 有效 IPv4
                    newLines.add(newDoIp6Line);        // 带注释的 do-ip6 行
                    newLines.addAll(ipv6Interfaces);   // 有效 IPv6
                    hasProcessedInterfaces = true;
                    continue;
                }

                // 3.3 退出 server 块
                if (!line.startsWith("        ")) {
                    inServerBlock = false;
                    // 若未处理，在块末尾补全
                    if (!hasProcessedInterfaces) {
                        newLines.addAll(ipv4Interfaces);
                        newLines.add("        do-ip6: " + (hasIPv6 ? "yes" : "no"));
                        newLines.addAll(ipv6Interfaces);
                    }
                }
            }

            newLines.add(line);
        }

        return newLines;
    }

    private boolean isValidIPv4(String ip) {
        return ip != null && !ip.trim().isEmpty();
    }

    private boolean isValidIPv6(String ip) {
        return ip != null && !ip.trim().isEmpty();
    }

    private String extractPureIp(String ipWithMask) {
        if (ipWithMask == null) return "";
        return ipWithMask.split("/")[0];
    }
}