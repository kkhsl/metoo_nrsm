package com.metoo.nrsm.core.utils.unbound.test;


import com.metoo.nrsm.core.dto.LocalDataDTO;
import com.metoo.nrsm.core.dto.LocalZoneDTO;
import com.metoo.nrsm.core.dto.UnboundDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class UnboundConfLocalZone {

    private static final String configFilePath = "C:\\Users\\hkk\\Desktop\\nrsm\\需求\\unbound\\unbound.conf";

    // 根据 validZoneNames 配置删除不必要的 local-zone 和 local-data 行，并新增缺失的行
    public static void updateConfigFile(String filePath, Set<String> validZoneNames, JSONArray localZoneConfig) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<String> updatedLines = new ArrayList<>();
        Set<String> existingZones = new HashSet<>();
        boolean skipLocalData = false;
        String currentIndentation = "";

        // 处理原文件中的每一行
        for (String line : lines) {
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));

            if (trimmedLine.startsWith("local-zone:") && trimmedLine.contains("\"")) {
                String zoneName = trimmedLine.split(":")[1].split(" ")[0].trim().replace("\"", "");
                if (!validZoneNames.contains(zoneName)) {
                    skipLocalData = true;
                    continue; // 删除该 local-zone 和 local-data 行
                } else {
                    skipLocalData = false;
                    existingZones.add(zoneName);
                    currentIndentation = indentation;
                    updatedLines.add(line);
                }
            } else if (skipLocalData && trimmedLine.startsWith("local-data:")) {
                continue; // 删除对应的 local-data 行
            } else {
                updatedLines.add(line); // 其他行不变
            }
        }

        // 新增缺失的 local-zone 和 local-data 行
        for (int i = 0; i < localZoneConfig.length(); i++) {
            JSONObject zoneConfig = localZoneConfig.getJSONObject(i);
            String zoneName = zoneConfig.getString("zoneName");

            if (!existingZones.contains(zoneName)) {
                // 构造并添加新的 local-zone 和 local-data
                updatedLines.add(currentIndentation + "local-zone: \"" + zoneName + "\" static");

                // 对于多个 local-data 处理
                JSONArray localDataArray = zoneConfig.getJSONArray("localData");
                for (int j = 0; j < localDataArray.length(); j++) {
                    JSONObject localDataItem = localDataArray.getJSONObject(j);
                    String hostName = localDataItem.getString("hostName");
                    String recordType = localDataItem.getString("recordType");
                    String mappedAddress = localDataItem.getString("mappedAddress");

                    updatedLines.add(currentIndentation + "local-data: \"" + hostName + " IN " + recordType + " " + mappedAddress + "\"");
                }
            } else {
                // 如果该 zone 已存在，则更新 local-data
                for (int j = 0; j < localZoneConfig.length(); j++) {
                    JSONObject zoneConfigUpdate = localZoneConfig.getJSONObject(j);
                    String existingZoneName = zoneConfigUpdate.getString("zoneName");

                    if (zoneName.equals(existingZoneName)) {
                        JSONObject localDataUpdate = zoneConfigUpdate.getJSONObject("localData");
                        JSONArray localDataArray = localDataUpdate.getJSONArray("localData");

                        // 查找该 local-zone 对应的 local-data 并更新
                        for (int k = 0; k < updatedLines.size(); k++) {
                            String lineUpdate = updatedLines.get(k).trim();
                            if (lineUpdate.startsWith("local-zone: \"" + zoneName + "\"") && k + 1 < updatedLines.size()) {
                                String localDataLine = updatedLines.get(k + 1).trim();
                                if (localDataLine.startsWith("local-data:")) {
                                    // 更新该 local-data
                                    for (int l = 0; l < localDataArray.length(); l++) {
                                        JSONObject localDataItem = localDataArray.getJSONObject(l);
                                        String hostName = localDataItem.getString("hostName");
                                        String recordType = localDataItem.getString("recordType");
                                        String mappedAddress = localDataItem.getString("mappedAddress");
                                        updatedLines.set(k + 1, currentIndentation + "local-data: \"" + hostName + " IN " + recordType + " " + mappedAddress + "\"");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 将更新后的内容写回配置文件
        Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void main(String[] args) {

        // 示例的 JSON 配置 (通常是从外部文件加载)
        String localZoneJson = "["
                + "    {"
                + "        \"zoneName\": \"bbb.com.\","
                + "        \"localData\": ["
                + "            {"
                + "                \"hostName\": \"www.bbb.com.\","
                + "                \"recordType\": \"A\","
                + "                \"mappedAddress\": \"192.168.6.201\""
                + "            },"
                + "            {"
                + "                \"hostName\": \"ftp.bbb.com.\","
                + "                \"recordType\": \"A\","
                + "                \"mappedAddress\": \"192.168.6.202\""
                + "            }"
                + "        ]"
                + "    },"
                + "    {"
                + "        \"zoneName\": \"aaa.com.\","
                + "        \"localData\": ["
                + "            {"
                + "                \"hostName\": \"www.aaa.com.\","
                + "                \"recordType\": \"A\","
                + "                \"mappedAddress\": \"192.168.5.203\""
                + "            }"
                + "        ]"
                + "    }"
                + "]";

        // 将 JSON 转为 JSONArray
        JSONArray localZoneConfig = new JSONArray(localZoneJson);

        // 定义有效的 zone 名称
        Set<String> validZoneNames = new HashSet<>(Arrays.asList("bbb.com.", "aaa.com."));

        Set<String> validForwardAddresses = new HashSet<>(Arrays.asList("223.5.5.5", "119.29.29.29", "2400:3200::1"));

        // 更新配置文件
        try {
            updateConfigFile(configFilePath, validZoneNames, localZoneConfig);
            System.out.println("配置文件更新成功！");
        } catch (IOException e) {
            System.err.println("更新配置文件时出错: " + e.getMessage());
        }
    }


    // 根据 validZoneNames 配置删除不必要的 local-zone 和 local-data 行，并新增缺失的行
    public static List<String> updateConfig(List<String> lines, Set<String> validZoneNames, JSONArray localZoneConfig) throws IOException {
        List<String> updatedLines = new ArrayList<>();
        Set<String> existingZones = new HashSet<>();
        boolean skipLocalData = false;
        String currentIndentation = "";

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
                    updatedLines.add(line);
                }
            } else if (skipLocalData && trimmedLine.startsWith("local-data:")) {
                continue; // 删除对应的 local-data 行
            } else {
                updatedLines.add(line); // 其他行不变
            }
        }

        if (existingZones.size() <= 0) {
            updatedLines.add(""); // 其他行不变
        }

        // 新增缺失的 local-zone 和 local-data 行
        for (int i = 0; i < localZoneConfig.length(); i++) {
            JSONObject zoneConfig = localZoneConfig.getJSONObject(i);
            String zoneName = zoneConfig.getString("zoneName");

            if (existingZones.size() <= 0 && i == 0) {

            }
            if (!existingZones.contains(zoneName)) {
                // 构造并添加新的 local-zone 和 local-data
                updatedLines.add(currentIndentation + "local-zone: \"" + zoneName + "\" static");

                // 对于多个 local-data 处理
                JSONArray localDataArray = zoneConfig.getJSONArray("localData");
                for (int j = 0; j < localDataArray.length(); j++) {
                    JSONObject localDataItem = localDataArray.getJSONObject(j);
                    String hostName = localDataItem.getString("hostName");
                    String recordType = localDataItem.getString("recordType");
                    String mappedAddress = localDataItem.getString("mappedAddress");

                    updatedLines.add(currentIndentation + "local-data: \"" + hostName + " IN " + recordType + " " + mappedAddress + "\"");
                }
            } else {
                // 如果该 zone 已存在，则更新 local-data
                for (int j = 0; j < localZoneConfig.length(); j++) {
                    JSONObject zoneConfigUpdate = localZoneConfig.getJSONObject(j);
                    String existingZoneName = zoneConfigUpdate.getString("zoneName");

                    if (zoneName.equals(existingZoneName)) {
                        JSONObject localDataUpdate = zoneConfigUpdate.getJSONObject("localData");
                        JSONArray localDataArray = localDataUpdate.getJSONArray("localData");

                        // 查找该 local-zone 对应的 local-data 并更新
                        for (int k = 0; k < updatedLines.size(); k++) {
                            String lineUpdate = updatedLines.get(k).trim();
                            if (lineUpdate.startsWith("local-zone: \"" + zoneName + "\"") && k + 1 < updatedLines.size()) {
                                String localDataLine = updatedLines.get(k + 1).trim();
                                if (localDataLine.startsWith("local-data:")) {
                                    // 更新该 local-data
                                    for (int l = 0; l < localDataArray.length(); l++) {
                                        JSONObject localDataItem = localDataArray.getJSONObject(l);
                                        String hostName = localDataItem.getString("hostName");
                                        String recordType = localDataItem.getString("recordType");
                                        String mappedAddress = localDataItem.getString("mappedAddress");
                                        updatedLines.set(k + 1, currentIndentation + "local-data: \"" + hostName + " IN " + recordType + " " + mappedAddress + "\"");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return updatedLines;
    }

    public static List<String> updateConfig(List<String> lines, Set<String> validZoneNames, List<LocalZoneDTO> localZoneConfig) throws IOException {
        List<String> updatedLines = new ArrayList<>();
        Set<String> existingZones = new HashSet<>();
        boolean skipLocalData = false;
        String currentIndentation = "";

        // 处理原文件中的每一行
        for (String line : lines) {
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));

            if (trimmedLine.startsWith("local-zone:") && trimmedLine.contains("\"")) {
                String zoneName = trimmedLine.split(":")[1].split(" ")[0].trim().replace("\"", "");
                if (!validZoneNames.contains(zoneName)) {
                    skipLocalData = true;
                    continue; // 删除该 local-zone 和 local-data 行
                } else {
                    skipLocalData = false;
                    existingZones.add(zoneName);
                    currentIndentation = indentation;
                    updatedLines.add(line);
                }
            } else if (skipLocalData && trimmedLine.startsWith("local-data:")) {
                continue; // 删除对应的 local-data 行
            } else {
                updatedLines.add(line); // 其他行不变
            }
        }

        if (existingZones.isEmpty()) {
            updatedLines.add(""); // 其他行不变
        }

        // 新增缺失的 local-zone 和 local-data 行
        for (int i = 0; i < localZoneConfig.size(); i++) {
            LocalZoneDTO zoneConfig = localZoneConfig.get(i);
            String zoneName = zoneConfig.getZoneName();

            if (existingZones.size() <= 0 && i == 0) {

            }
            if (!existingZones.contains(zoneName)) {
                // 构造并添加新的 local-zone 和 local-data
                updatedLines.add(currentIndentation + "local-zone: \"" + zoneName + "\" static");

                // 对于多个 local-data 处理
                List<LocalDataDTO> localDataList = zoneConfig.getLocalData();
                for (int j = 0; j < localDataList.size(); j++) {
                    LocalDataDTO localDataItem = localDataList.get(j);
                    String hostName = localDataItem.getHostName();
                    String recordType = localDataItem.getRecordType();
                    String mappedAddress = localDataItem.getMappedAddress();

                    updatedLines.add(currentIndentation + "local-data: \"" + hostName + " IN " + recordType + " " + mappedAddress + "\"");
                }
            } else {
                // 如果该 zone 已存在，则更新 local-data
                for (int j = 0; j < localZoneConfig.size(); j++) {
                    LocalZoneDTO zoneConfigUpdate = localZoneConfig.get(i);
                    String existingZoneName = zoneConfigUpdate.getZoneName();

                    if (zoneName.equals(existingZoneName)) {
                        List<LocalDataDTO> localDataList = zoneConfig.getLocalData();

                        // 查找该 local-zone 对应的 local-data 并更新
                        for (int k = 0; k < updatedLines.size(); k++) {
                            String lineUpdate = updatedLines.get(k).trim();
                            if (lineUpdate.startsWith("local-zone: \"" + zoneName + "\"") && k + 1 < updatedLines.size()) {
                                String localDataLine = updatedLines.get(k + 1).trim();
                                if (localDataLine.startsWith("local-data:")) {
                                    // 更新该 local-data
                                    for (int l = 0; l < localDataList.size(); l++) {
                                        LocalDataDTO localDataItem = localDataList.get(l);
                                        String hostName = localDataItem.getHostName();
                                        String recordType = localDataItem.getRecordType();
                                        String mappedAddress = localDataItem.getMappedAddress();

                                        updatedLines.set(k + 1, currentIndentation + "local-data: \"" + hostName + " IN " + recordType + " " + mappedAddress + "\"");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return updatedLines;
    }

}
