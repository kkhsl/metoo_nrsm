package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.LocalDataDTO;
import com.metoo.nrsm.core.dto.LocalZoneDTO;
import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalZoneUpdateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) throws IOException {
        List<LocalZoneDTO> localZoneConfig = (List<LocalZoneDTO>) configData;
        // 处理 local-zone 的更新逻辑
        List<String> updatedLines = updateLines(lines, localZoneConfig);
        return updatedLines;
    }

    public static List<String> updateLines(List<String> lines, List<LocalZoneDTO> localZoneDTOS) throws IOException {
        // 记录有效的 zone 名称
        Set<String> validZoneNames = new HashSet<>();
        if (localZoneDTOS != null && !localZoneDTOS.isEmpty()) {
            localZoneDTOS.forEach(e -> {
                String zoneName = e.getZoneName();
                if (StringUtil.isNotEmpty(zoneName)) {
                    validZoneNames.add(zoneName);
                }
            });
        }

        // 存储更新后的行
        List<String> updatedLines = new ArrayList<>();
        // 记录已处理的 zones
        Set<String> existingZones = new HashSet<>();
        // 当前的缩进
        String currentIndentation = "";
        // 跳过 local-data 行的标志
        boolean skipLocalData = false;

        // 处理原文件中的每一行
        for (String line : lines) {
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));

            if (trimmedLine.startsWith("local-zone:") && trimmedLine.contains("\"")) {
                String zoneName = trimmedLine.split(":")[1].trim().split(" ")[0].trim().replace("\"", "");
                if (!validZoneNames.contains(zoneName)) {
                    skipLocalData = true;
                    continue; // 删除该 local-zone 和 local-data 行
                } else {
                    skipLocalData = false;
                    existingZones.add(zoneName);
                    currentIndentation = indentation;
                    updatedLines.add(line); // 保留该 local-zone
                }
            } else if (skipLocalData && trimmedLine.startsWith("local-data:")) {
                continue; // 删除对应的 local-data 行
            } else {
                updatedLines.add(line); // 其他行不变
            }
        }

        // 如果没有有效的 zones，添加一个空行
        if (existingZones.isEmpty()) {
            updatedLines.add("");
        }

        // 新增缺失的 local-zone 和 local-data 行
        List<String> newLocalZoneLines = new ArrayList<>();
        for (LocalZoneDTO zoneConfig : localZoneDTOS) {
            String zoneName = zoneConfig.getZoneName();

            // 如果 zone 已经存在，则直接更新，否则新增
            if (!existingZones.contains(zoneName)) {
                newLocalZoneLines.add(currentIndentation + "local-zone: \"" + zoneName + "\" static");

                // 遍历 local-data 配置，确保不重复插入
                Set<String> existingHostNames = new HashSet<>();
                for (LocalDataDTO localDataItem : zoneConfig.getLocalData()) {
                    String hostName = localDataItem.getHostName();
                    // 检查是否已经添加过该 hostName
                    if (!existingHostNames.contains(hostName)) {
                        newLocalZoneLines.add(currentIndentation + "local-data: \"" + hostName + " IN "
                                + localDataItem.getRecordType() + " " + localDataItem.getMappedAddress() + "\"");
                        existingHostNames.add(hostName);
                    }
                }
            } else {
                // 如果该 zone 已存在，则更新 local-data
                updateLocalData(updatedLines, zoneName, zoneConfig.getLocalData(), currentIndentation);
            }
        }

        // 找到 remote-control 行的位置，并在其上方插入新配置
        int remoteControlIndex = -1;
        for (int i = 0; i < updatedLines.size(); i++) {
            if (updatedLines.get(i).trim().startsWith("remote-control:")) {
                remoteControlIndex = i;
                break;
            }
        }

        // 插入 local-zone 和 local-data 行
        if (remoteControlIndex != -1) {
            updatedLines.addAll(remoteControlIndex, newLocalZoneLines);
        } else {
            // 如果没有找到 remote-control，则直接添加到末尾
            updatedLines.addAll(newLocalZoneLines);
        }

        return updatedLines;
    }

    // 更新已有的 local-data 行
    private static void updateLocalData(List<String> updatedLines, String zoneName, List<LocalDataDTO> newLocalData, String currentIndentation) {
        Set<String> newHostNames = new HashSet<>();
        for (LocalDataDTO localData : newLocalData) {
            newHostNames.add(localData.getHostName());
        }

        // 遍历更新后的行，查找对应的 local-zone 和 local-data 进行更新
        boolean zoneFound = false; // 标记是否找到 zone
        for (int i = 0; i < updatedLines.size(); i++) {
            String line = updatedLines.get(i).trim();
            if (line.startsWith("local-zone: \"" + zoneName + "\"")) {
                zoneFound = true;
                int j = i + 1;
                while (j < updatedLines.size()) {
                    String localDataLine = updatedLines.get(j).trim();
                    if (localDataLine.startsWith("local-data:")) {
                        String hostName = localDataLine.split(":")[1].trim().split(" ")[0].trim().replace("\"", "");
                        if (newHostNames.contains(hostName)) {
                            // 更新该 local-data
                            for (LocalDataDTO localData : newLocalData) {
                                if (localData.getHostName().equals(hostName)) {
                                    updatedLines.set(j, currentIndentation + "local-data: \"" + localData.getHostName() + " IN "
                                            + localData.getRecordType() + " " + localData.getMappedAddress() + "\"");
                                }
                            }
                        } else {
                            // 删除无用的 local-data
                            updatedLines.remove(j);
                            j--; // 删除后需要回退一步，继续检查新的行
                        }
                    } else {
                        break; // 找到下一个 local-zone 或结束
                    }
                    j++;
                }
            }

        }
    }
}
