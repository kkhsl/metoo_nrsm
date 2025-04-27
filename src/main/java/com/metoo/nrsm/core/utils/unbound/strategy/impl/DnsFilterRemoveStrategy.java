package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.util.List;
import java.util.stream.Collectors;

// 新增删除策略实现
public class DnsFilterRemoveStrategy implements ConfigUpdateStrategy {
    
    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        String domainToRemove = (String) configData;
        return processLines(lines, domainToRemove);
    }

    private List<String> processLines(List<String> original, String domain) {
        return original.stream()
            .filter(line -> !isTargetLine(line, domain))
            .collect(Collectors.toList());
    }

    private boolean isTargetLine(String line, String domain) {
        String trimmed = line.trim();
        return trimmed.startsWith("local-zone:") 
            && trimmed.contains("\"" + domain + "\"")
            && trimmed.endsWith("refuse");
    }
}