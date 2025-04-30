package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntefaceUpdateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) throws IOException {
        Set<String> validForwardAddresses = (Set<String>) configData;
        // 处理 forward-zone 的更新逻辑
        List<String> updatedLines = updateLines(lines, validForwardAddresses);
        return updatedLines;
    }

    public List<String> updateLines(List<String> lines, Set<String> ips) throws IOException {
        // 修改 interface 配置
        List<String> newConfigLines = new ArrayList<>();

        boolean isIpv6Enabled = false;
        boolean addedInterfaces = false; // 记录是否已经添加过新的接口配置
        String indentation = "";
        for (String line : lines) {
            if (line.trim().startsWith("do-ip6: yes")) {
                isIpv6Enabled = true;
                newConfigLines.add(line);

                String trimmedLine = line.trim();
                indentation = line.substring(0, line.indexOf(trimmedLine));
                continue;
            }
            // 删除所有 interface 配置行
            if (isIpv6Enabled && line.trim().startsWith("interface:")) {
                continue; // 忽略 interface 配置
            }

            // 如果有新的 IP 地址需要添加 interface 配置
            if (isIpv6Enabled && !addedInterfaces) {
                if (ips.size() > 0 && !line.trim().startsWith("interface:")) {
                    for (String ip : ips) {
                        String newInterfaceLine = indentation + "interface: " + "::";
                        newConfigLines.add(newInterfaceLine); // 添加新的 interface 配置
                    }
                    addedInterfaces = true; // 标记已添加过接口配置
                }
            }
            // 添加其他未修改的行
            newConfigLines.add(line);
        }
//        restartUnboundService();

        // 返回更新后的内容
        return newConfigLines;
    }

}
