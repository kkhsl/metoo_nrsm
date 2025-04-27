package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;
import com.metoo.nrsm.core.vo.DnsFilterUpdatePayload;
import com.metoo.nrsm.entity.DnsFilter;

import java.util.ArrayList;
import java.util.List;

public class DnsFilterUpdateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        DnsFilterUpdatePayload payload = (DnsFilterUpdatePayload) configData;
        return processLines(lines, payload);
    }

    private List<String> processLines(List<String> original, DnsFilterUpdatePayload payload) {
        List<String> updated = new ArrayList<>();
        boolean foundOld = false;
        String newLine = buildConfigLine(payload.getNewConfig());
        String oldDomain = payload.getOldDomain();

        for (String line : original) {
            // 匹配旧域名配置行
            if (oldDomain != null && isDomainLine(line, oldDomain)) {
                updated.add(newLine); // 替换旧行
                foundOld = true;
            }
            // 避免重复新配置
            else if (!isDomainLine(line, payload.getNewConfig().getDomainName())) {
                updated.add(line);
            }
        }

        // 新增配置（未找到旧记录且不存在新配置时）
        if (!foundOld && !containsDomain(updated, payload.getNewConfig().getDomainName())) {
            updated.add(newLine);
        }

        return updated;
    }

    private String buildConfigLine(DnsFilter config) {
        return String.format("local-zone: \"%s\" refuse", config.getDomainName());
    }

    private boolean isDomainLine(String line, String domain) {
        return line.trim().startsWith("local-zone:")
                && line.contains("\"" + domain + "\"");
    }

    private boolean containsDomain(List<String> lines, String domain) {
        return lines.stream().anyMatch(line -> isDomainLine(line, domain));
    }

   /* public static List<String> updateLines(List<String> lines, DnsFilter dnsFilter) {
        List<String> updatedLines = new ArrayList<>();
        String configEntry = String.format("local-zone: \"%s\" refuse", dnsFilter.getDomainName());
        boolean inserted = false;

        for (String line : lines) {
            // 在# Blocked domains注释后插入
            if (!inserted && line.startsWith("# Blocked domains")) {
                updatedLines.add(line);
                updatedLines.add(configEntry);
                inserted = true;
            } else {
                updatedLines.add(line);
            }
        }

        // 如果没找到注释则追加到文件末尾
        if (!inserted) {
            updatedLines.add("# Blocked domains");
            updatedLines.add(configEntry);
        }

        return updatedLines;
    }*/
}
