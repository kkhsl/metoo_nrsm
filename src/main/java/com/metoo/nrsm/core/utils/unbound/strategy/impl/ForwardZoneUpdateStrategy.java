package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.io.IOException;
import java.util.*;

public class ForwardZoneUpdateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) throws IOException {
        Set<String> validForwardAddresses = (Set<String>) configData;
        // 处理 forward-zone 的更新逻辑
        List<String> updatedLines = updateLines(lines, validForwardAddresses);
        return updatedLines;
    }

    public List<String> updateLines(List<String> lines, Set<String> validForwardAddresses) throws IOException {
        // 读取配置文件内容
        List<String> updatedLines = new ArrayList<>();
        boolean forwardZoneSectionFound = false;
        boolean skipForwardZone = false;

        // 存储当前文件中所有的 forward-addr
        Set<String> existingAddresses = new HashSet<>();

        // 遍历文件的每一行
        for (String line : lines) {
            String trimmedLine = line.trim();

            if(trimmedLine.startsWith("forward-zone:") && validForwardAddresses.isEmpty()){
                skipForwardZone = true; // 设置跳过标志，后续的行会被跳过
                continue; // 跳过当前 forward-zone 配置块
            }

            if(skipForwardZone){
                skipForwardZone = false;
                continue;
            }
            if (trimmedLine.startsWith("forward-zone:")) {
                forwardZoneSectionFound = true;
                updatedLines.add(line); // 保留 forward-zone 配置块的起始部分
            } else if (forwardZoneSectionFound && trimmedLine.startsWith("forward-addr:")) {
                String address = trimmedLine.split(" ")[1].trim();
                existingAddresses.add(address); // 将文件中存在的 forward-addr 地址加入集合
                updatedLines.add(line); // 保留现有的 forward-addr
            } else {
                updatedLines.add(line); // 保留其他未修改的部分
            }
        }

        // 如果没有找到 forward-zone 配置块，添加它
        if(!validForwardAddresses.isEmpty()){
            if (!forwardZoneSectionFound) {
                updatedLines.add("\nforward-zone:");
                updatedLines.add("  name: \".\"");
            }
        }
        // 删除不需要的 forward-addr 地址
        ListIterator<String> iterator = updatedLines.listIterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.trim().startsWith("forward-addr:")) {
                String address = line.trim().split(" ")[1].trim();
                if (!validForwardAddresses.contains(address)) {
                    iterator.remove(); // 如果地址不在 validForwardAddresses 中，删除该行
                }
            }
        }

        // 添加缺失的 forward-addr 地址
        for (String address : validForwardAddresses) {
            if (!existingAddresses.contains(address)) {
                updatedLines.add("  forward-addr: " + address);
            }
        }

        // 写回更新后的内容到配置文件
        return updatedLines;
    }
}
