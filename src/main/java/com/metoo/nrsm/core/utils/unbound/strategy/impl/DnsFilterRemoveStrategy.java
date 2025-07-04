package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DnsFilterRemoveStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        if (configData instanceof Collection) {
            return batchRemove(lines, (Collection<String>) configData);
        }
        return singleRemove(lines, (String) configData);
    }

    private List<String> batchRemove(List<String> original, Collection<String> domains) {
        Set<String> domainSet = new HashSet<>(domains);
        return original.stream()
                .filter(line -> !isAnyDomainLine(line, domainSet))
                .collect(Collectors.toList());
    }

    private List<String> singleRemove(List<String> original, String domain) {
        return original.stream()
                .filter(line -> !isDomainLine(line, domain))
                .collect(Collectors.toList());
    }

    private boolean isAnyDomainLine(String line, Set<String> domains) {
        return domains.stream().anyMatch(domain -> isDomainLine(line, domain));
    }

    /**
     * 增强匹配逻辑：支持识别被注释的配置行
     * 示例匹配：
     * local-zone: "iqiyi.com" refuse
     * #local-zone: "iqiyi.com" refuse
     * # local-zone:  "iqiyi.com"  refuse
     */
    private boolean isDomainLine(String line, String domain) {
        // 移除行首的注释符号和空格
        String processed = line.trim().replaceAll("^#+\\s*", "");
        return processed.startsWith("local-zone:")
                && processed.contains("\"" + domain + "\"")
                && processed.trim().endsWith("refuse");
    }
}