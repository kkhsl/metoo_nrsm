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

        try {
            List<LocalZoneDTO> localZoneDTOS = new ObjectMapper().readValue(unbound.getLocalZone(), new TypeReference<List<LocalZoneDTO>>() {});
            Set<String> forwardAddress = new ObjectMapper().readValue(unbound.getForwardAddress(), Set.class);
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("private-address", lines, unbound.getPrivateAddress());
            lines = configUpdater.updateConfig("forward-zone", lines, forwardAddress);
            lines = configUpdater.updateConfig("local-zone", lines, localZoneDTOS);

            // 将更新后的内容写回配置文件
            try {
                Files.write(Paths.get(filePath), lines, StandardOpenOption.TRUNCATE_EXISTING);
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


    private static final String configFilePath = "C:\\Users\\hkk\\Desktop\\nrsm\\需求\\unbound\\unbound.conf";

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
