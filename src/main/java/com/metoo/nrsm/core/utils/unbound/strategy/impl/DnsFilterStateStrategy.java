package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;
import com.metoo.nrsm.core.vo.DnsFilterStatePayload;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DnsFilterStateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        DnsFilterStatePayload payload = (DnsFilterStatePayload) configData;
        return processLines(lines, payload);
    }

    private List<String> processLines(List<String> original, DnsFilterStatePayload payload) {
        String domain = payload.getDomain();
        boolean shouldEnable = payload.isEnable();
        Pattern pattern = Pattern.compile("^(#?)\\s*local-zone:\\s*\"" + domain + "\"\\s+refuse");

        return original.stream()
                .map(line -> {
                    Matcher matcher = pattern.matcher(line.trim());
                    if (matcher.matches()) {
                        // 根据状态重建行
                        return buildConfigLine(shouldEnable, domain);
                    }
                    return line;
                })
                .collect(Collectors.toList());
    }

    private String buildConfigLine(boolean enabled, String domain) {
        return (enabled ? "" : "# ") + "local-zone: \"" + domain + "\" refuse";
    }
}