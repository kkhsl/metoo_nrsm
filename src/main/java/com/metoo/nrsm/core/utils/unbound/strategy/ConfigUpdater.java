package com.metoo.nrsm.core.utils.unbound.strategy;

import com.metoo.nrsm.core.utils.unbound.strategy.impl.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigUpdater {

    private Map<String, ConfigUpdateStrategy> strategyMap;

    public ConfigUpdater() {
        strategyMap = new HashMap<>();
        strategyMap.put("forward-zone", new ForwardZoneUpdateStrategy());
        strategyMap.put("local-zone", new LocalZoneUpdateStrategy());
        strategyMap.put("private-address", new PrivateAddressUpdateStrategy());
        strategyMap.put("interface", new IntefaceUpdateStrategy());
        strategyMap.put("dns-filter", new DnsFilterUpdateStrategy());
        strategyMap.put("dns-filter-remove", new DnsFilterRemoveStrategy());
        strategyMap.put("dns-filter-enable", new DnsFilterStateStrategy());
        strategyMap.put("dns-filter-disable", new DnsFilterStateStrategy());
    }

    public List<String> updateConfig(String configType, List<String> lines, Object configData) throws IOException {
        ConfigUpdateStrategy strategy = strategyMap.get(configType);
        if (strategy != null) {
            return strategy.updateConfig(lines, configData);
        } else {
            throw new UnsupportedOperationException("不支持的配置类型: " + configType);
        }
    }
}
