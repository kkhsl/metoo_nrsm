package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.util.List;
import java.util.stream.Collectors;

public class ChronyTimeStateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) {
        String poolAddress = (String) configData;
        if (poolAddress == null || poolAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Pool address must not be null or empty");
        }

        // 移除所有现有的未注释的pool行
        List<String> modifiedLines = lines.stream()
                .filter(line -> {
                    String trimmed = line.trim();
                    return !trimmed.startsWith("pool ");
                })
                .collect(Collectors.toList());

        // 构建新的pool配置行
        String newPoolLine = "pool " + poolAddress + " iburst";

        // 确定插入位置：在注释块之后的第一行
        int insertIndex = findInsertionIndex(modifiedLines);

        // 插入新的pool行
        modifiedLines.add(insertIndex, newPoolLine);

        return modifiedLines;
    }

    private int findInsertionIndex(List<String> lines) {
        // 查找连续的注释块，通常位于文件顶部
        int lastCommentLine = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().startsWith("#")) {
                lastCommentLine = i;
            } else {
                // 遇到第一个非注释行时停止
                break;
            }
        }
        // 插入位置为注释块之后，若没有注释则插入到文件顶部
        return lastCommentLine != -1 ? lastCommentLine + 1 : 0;
    }
}