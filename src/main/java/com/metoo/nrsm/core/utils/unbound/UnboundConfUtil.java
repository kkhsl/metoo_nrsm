package com.metoo.nrsm.core.utils.unbound;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.dto.LocalZoneDTO;
import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdater;
import com.metoo.nrsm.core.utils.unbound.test.UnboundConfForward;
import com.metoo.nrsm.core.utils.unbound.test.UnboundConfLocalZone;
import com.metoo.nrsm.core.utils.unbound.test.UnboundConfPrivateAddress;
import com.metoo.nrsm.entity.Unbound;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class UnboundConfUtil {


    public static boolean updateConfigFile(String filePath, Set<String> validZoneNames, List<LocalZoneDTO> localZoneConfig,
                                           Set<String> validForwardAddresses, boolean privateAddress) throws IOException {

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                updatedLines.add(line);
            }
            updatedLines = UnboundConfPrivateAddress.updateConfig(updatedLines, privateAddress);

            updatedLines = UnboundConfForward.updateConfig(updatedLines, validForwardAddresses);

            updatedLines = UnboundConfLocalZone.updateConfig(updatedLines, validZoneNames, localZoneConfig);

            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateConfigFile(String filePath, Unbound unbound) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 解析数据
            List<LocalZoneDTO> localZoneDTOS = objectMapper.readValue(unbound.getLocalZone(), new TypeReference<List<LocalZoneDTO>>() {});
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 更新配置
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("local-zone", lines, localZoneDTOS);

            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), lines, StandardOpenOption.TRUNCATE_EXISTING);
                return true; // 更新成功
            } catch (IOException e) {
                System.err.println("写入配置文件时发生错误: " + e.getMessage());
                return false; // 写入失败
            }
        } catch (IOException e) {
            System.err.println("更新配置文件时发生错误: " + e.getMessage());
            return false; // 解析或读取失败
        }
    }
    public static boolean updateConfigDNSFile(String filePath, Unbound unbound) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 解析数据
            Set<String> forwardAddress = objectMapper.readValue(unbound.getForwardAddress(), new TypeReference<Set<String>>() {});
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 更新配置
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("forward-zone", lines, forwardAddress);
            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), lines, StandardOpenOption.TRUNCATE_EXISTING);
                return true; // 更新成功
            } catch (IOException e) {
                System.err.println("写入配置文件时发生错误: " + e.getMessage());
                return false; // 写入失败
            }
        } catch (IOException e) {
            System.err.println("更新配置文件时发生错误: " + e.getMessage());
            return false; // 解析或读取失败
        }
    }

    public static boolean updateConfigAdressFile(String filePath, Unbound unbound) throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            // 更新配置
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("private-address", lines, unbound.getPrivateAddress());
            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), lines, StandardOpenOption.TRUNCATE_EXISTING);
                return true; // 更新成功
            } catch (IOException e) {
                System.err.println("写入配置文件时发生错误: " + e.getMessage());
                return false; // 写入失败
            }
        } catch (IOException e) {
            System.err.println("更新配置文件时发生错误: " + e.getMessage());
            return false; // 解析或读取失败
        }
    }


    public static boolean deleteConfigFile(String filePath, Unbound unbound) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 解析数据
            List<LocalZoneDTO> localZoneDTOS = objectMapper.readValue(unbound.getLocalZone(), new TypeReference<List<LocalZoneDTO>>() {});
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 收集要删除的 zone 名称
            Set<String> zonesToDelete = new HashSet<>();
            for (LocalZoneDTO zone : localZoneDTOS) {
                zonesToDelete.add(zone.getZoneName());
            }

            // 删除配置
            List<String> updatedLines = removeLocalZoneAndData(lines, zonesToDelete);

            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
                return true; // 更新成功
            } catch (IOException e) {
                System.err.println("写入配置文件时发生错误: " + e.getMessage());
                return false; // 写入失败
            }
        } catch (IOException e) {
            System.err.println("更新配置文件时发生错误: " + e.getMessage());
            return false; // 解析或读取失败
        }
    }

    private static List<String> removeLocalZoneAndData(List<String> lines, Set<String> zonesToDelete) {
        boolean skipLocalData = false;
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("local-zone:")) {
                String zoneName = trimmedLine.split(":")[1].trim().split(" ")[0].replace("\"", "");
                if (zonesToDelete.contains(zoneName)) {
                    skipLocalData = true; // 设置标志以跳过后续的 local-data 行
                    continue; // 跳过该 local-zone 行
                }
            }
            // 处理 local-data 行
            else if (skipLocalData && trimmedLine.startsWith("local-data:")) {
                continue; // 跳过对应的 local-data 行
            }
            // 如果不是要删除的配置，则保留该行
            else {
                updatedLines.add(line);
            }

            // 如果遇到下一个 local-zone，重置标志
            if (trimmedLine.startsWith("local-zone:") && !zonesToDelete.contains(trimmedLine.split(":")[1].trim().split(" ")[0].replace("\"", ""))) {
                skipLocalData = false;
            }
        }

        return updatedLines;
    }


    public static boolean updateAllConfigFile(String filePath, Unbound unbound) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 解析数据
            List<LocalZoneDTO> localZoneDTOS = objectMapper.readValue(unbound.getLocalZone(), new TypeReference<List<LocalZoneDTO>>() {});
            Set<String> forwardAddress = objectMapper.readValue(unbound.getForwardAddress(), new TypeReference<Set<String>>() {});
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 更新配置
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("private-address", lines, unbound.getPrivateAddress());
            lines = configUpdater.updateConfig("forward-zone", lines, forwardAddress);
            lines = configUpdater.updateConfig("local-zone", lines, localZoneDTOS);

            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), lines, StandardOpenOption.TRUNCATE_EXISTING);
                return true; // 更新成功
            } catch (IOException e) {
                System.err.println("写入配置文件时发生错误: " + e.getMessage());
                return false; // 写入失败
            }
        } catch (IOException e) {
            System.err.println("更新配置文件时发生错误: " + e.getMessage());
            return false; // 解析或读取失败
        }
    }


    private static final String configFilePath = "C:\\Users\\leo\\Desktop\\unbound.conf";

    // 根据 validZoneNames 配置删除不必要的 local-zone 和 local-data 行，并新增缺失的行
    public static boolean updateConfigFileTest(String filePath, Set<String> validZoneNames, JSONArray localZoneConfig,
                                        Set<String> validForwardAddresses, boolean privateAddress) throws IOException {

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                updatedLines.add(line);
            }
            updatedLines = UnboundConfPrivateAddress.updateConfig(updatedLines, privateAddress);

            updatedLines = UnboundConfForward.updateConfig(updatedLines, validForwardAddresses);

            updatedLines = UnboundConfLocalZone.updateConfig(updatedLines, validZoneNames, localZoneConfig);

            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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
            updateConfigFileTest(configFilePath, validZoneNames, localZoneConfig, validForwardAddresses, true);
            System.out.println("配置文件更新成功！");
        } catch (IOException e) {
            System.err.println("更新配置文件时出错: " + e.getMessage());
        }
    }
}