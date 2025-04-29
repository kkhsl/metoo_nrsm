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
        List<String> updatedLines = updateLines(lines, localZoneConfig);
        return updatedLines;
    }

    public static List<String> updateLines(List<String> lines, List<LocalZoneDTO> localZoneDTOS) throws IOException {
        Set<String> validZoneNames = new HashSet<>();
        if (localZoneDTOS != null && !localZoneDTOS.isEmpty()) {
            localZoneDTOS.forEach(e -> {
                String zoneName = e.getZoneName();
                if (StringUtil.isNotEmpty(zoneName)) {
                    validZoneNames.add(zoneName);
                }
            });
        }

        List<String> updatedLines = new ArrayList<>();
        Set<String> existingZones = new HashSet<>();
        String currentIndentation = "";
        boolean skipLocalData = false;

        for (String line : lines) {
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));

            if (trimmedLine.startsWith("local-zone:") && trimmedLine.contains("\"")) {
                String zoneName = trimmedLine.split(":")[1].trim().split(" ")[0].trim().replace("\"", "");
                if (!validZoneNames.contains(zoneName)) {
                    skipLocalData = true;
                    continue;
                } else {
                    skipLocalData = false;
                    existingZones.add(zoneName);
                    currentIndentation = indentation;
                    updatedLines.add(line);
                }
            } else if (skipLocalData && trimmedLine.startsWith("local-data:")) {
                continue;
            } else {
                updatedLines.add(line);
            }
        }

        if (existingZones.isEmpty()) {
            updatedLines.add("");
        }

        List<String> newLocalZoneLines = new ArrayList<>();
        for (LocalZoneDTO zoneConfig : localZoneDTOS) {
            String zoneName = zoneConfig.getZoneName();

            if (!existingZones.contains(zoneName)) {
                newLocalZoneLines.add(currentIndentation + "local-zone: \"" + zoneName + "\" static");
                // 添加所有 local-data 条目，不基于 hostName 去重
                for (LocalDataDTO localDataItem : zoneConfig.getLocalData()) {
                    newLocalZoneLines.add(currentIndentation + "local-data: \"" + localDataItem.getHostName() + " IN "
                            + localDataItem.getRecordType() + " " + localDataItem.getMappedAddress() + "\"");
                }
            } else {
                updateLocalData(updatedLines, zoneName, zoneConfig.getLocalData(), currentIndentation);
            }
        }

        int remoteControlIndex = -1;
        for (int i = 0; i < updatedLines.size(); i++) {
            if (updatedLines.get(i).trim().startsWith("remote-control:")) {
                remoteControlIndex = i;
                break;
            }
        }

        if (remoteControlIndex != -1) {
            updatedLines.addAll(remoteControlIndex, newLocalZoneLines);
        } else {
            updatedLines.addAll(newLocalZoneLines);
        }

        return updatedLines;
    }

    private static void updateLocalData(List<String> updatedLines, String zoneName, List<LocalDataDTO> newLocalData, String currentIndentation) {
        // 收集所有新数据的唯一键（host + type + address）
        Set<String> newDataKeys = new HashSet<>();
        for (LocalDataDTO data : newLocalData) {
            String key = data.getHostName() + "|" + data.getRecordType() + "|" + data.getMappedAddress();
            newDataKeys.add(key);
        }

        boolean zoneFound = false;
        for (int i = 0; i < updatedLines.size(); i++) {
            String line = updatedLines.get(i).trim();
            if (line.startsWith("local-zone: \"" + zoneName + "\"")) {
                zoneFound = true;
                int j = i + 1;
                Set<String> existingDataKeys = new HashSet<>();

                // 遍历当前 zone 下的所有 local-data 行
                while (j < updatedLines.size()) {
                    String localDataLine = updatedLines.get(j).trim();
                    if (localDataLine.startsWith("local-data:")) {
                        // 解析现有行的 host、type、address
                        String dataPart = localDataLine.split(":")[1].trim().replace("\"", "");
                        String[] parts = dataPart.split("\\s+");
                        String host = parts[0];
                        String type = parts[1];
                        String address = parts[2];
                        String existingKey = host + "|" + type + "|" + address;

                        existingDataKeys.add(existingKey);

                        // 如果当前行不在新数据中，则删除
                        if (!newDataKeys.contains(existingKey)) {
                            updatedLines.remove(j);
                        } else {
                            // 保留并移除已处理的key
                            newDataKeys.remove(existingKey);
                            j++;
                        }
                    } else {
                        break;
                    }
                }

                // 添加剩余的新数据
                for (LocalDataDTO data : newLocalData) {
                    String key = data.getHostName() + "|" + data.getRecordType() + "|" + data.getMappedAddress();
                    if (newDataKeys.contains(key)) {
                        updatedLines.add(j, currentIndentation + "local-data: \"" + data.getHostName() + " IN "
                                + data.getRecordType() + " " + data.getMappedAddress() + "\"");
                        j++;
                        newDataKeys.remove(key);
                    }
                }
            }
        }
    }
}
