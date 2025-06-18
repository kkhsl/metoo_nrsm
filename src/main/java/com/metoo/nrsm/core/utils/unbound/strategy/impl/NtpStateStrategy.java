package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.util.ArrayList;
import java.util.List;

public class NtpStateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        List<String> instances = (List<String>) configData;
        List<String> modifiedLines = new ArrayList<>();
        int lastPoolIndex = -1;

        // 1. 遍历原配置，移除所有未注释的 allow 行，并记录最后一个 pool 行位置
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 记录最后一个 pool 行的位置
            if (trimmed.startsWith("pool ")) {
                lastPoolIndex = modifiedLines.size(); // 当前 modifiedLines 的索引
            }

            // 跳过未注释的 allow 行
            if (trimmed.startsWith("allow ")) {
                continue;
            }

            modifiedLines.add(line); // 保留其他行
        }

        // 2. 确定插入位置：在最后一个 pool 行之后，若无则插入到文件末尾
        int insertIndex = (lastPoolIndex != -1) ? lastPoolIndex + 1 : modifiedLines.size();

        // 3. 插入新的 allow 行（如果实例列表非空）
        if (instances != null && !instances.isEmpty()) {
            List<String> newAllowLines = new ArrayList<>();
            for (String ip : instances) {
                newAllowLines.add("allow " + ip);
            }
            modifiedLines.addAll(insertIndex, newAllowLines);
        }

        return modifiedLines;
    }
}