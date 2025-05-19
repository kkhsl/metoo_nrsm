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

        // 1. 提取有效接口
        List<String> ipv4Interfaces = interfaces.stream()
                .filter(intf -> isValidIPv4(intf.getIpv4address()))
                .map(intf -> "        interface: " + intf.getIpv4address())
                .collect(Collectors.toList());

        List<String> ipv6Interfaces = interfaces.stream()
                .filter(intf -> isValidIPv6(intf.getIpv6address()))
                .map(intf -> "        interface: " + intf.getIpv6address())
                .collect(Collectors.toList());

        // 判断是否需要启用 IPv6
        boolean hasIPv6 = !ipv6Interfaces.isEmpty();

        // 2. 重构配置
        List<String> newLines = new ArrayList<>();
        boolean inServerBlock = false;
        boolean hasProcessedInterfaces = false;

        for (String line : lines) {
            // 2.1 识别 server 块开始
            if (line.trim().startsWith("server:")) {
                inServerBlock = true;
                newLines.add(line);
                continue;
            }

            // 2.2 处理 server 块内部
            if (inServerBlock) {
                // 2.2.1 跳过所有旧接口行
                if (line.trim().startsWith("interface:")) {
                    continue;
                }

                // 2.2.2 找到 do-ip6 行时插入接口
                if (line.trim().startsWith("do-ip6:")) {
                    newLines.addAll(ipv4Interfaces);  // IPv4 在 do-ip6 前
                    newLines.add(line);               // 保留原 do-ip6 行
                    newLines.addAll(ipv6Interfaces);  // IPv6 在 do-ip6 后
                    hasProcessedInterfaces = true;
                    continue;
                }

                // 2.2.3 识别 server 块结束（遇到非缩进行）
                if (!line.startsWith("    ")) {
                    inServerBlock = false;
                    // 若未插入接口
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
}