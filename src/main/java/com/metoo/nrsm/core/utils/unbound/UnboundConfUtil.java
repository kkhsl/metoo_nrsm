package com.metoo.nrsm.core.utils.unbound;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.dto.LocalZoneDTO;
import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdater;
import com.metoo.nrsm.core.utils.unbound.test.UnboundConfForward;
import com.metoo.nrsm.core.utils.unbound.test.UnboundConfLocalZone;
import com.metoo.nrsm.core.utils.unbound.test.UnboundConfPrivateAddress;
import com.metoo.nrsm.core.vo.DnsFilterStatePayload;
import com.metoo.nrsm.core.vo.DnsFilterUpdatePayload;
import com.metoo.nrsm.entity.DnsFilter;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Unbound;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

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

    // 修正写入逻辑
    public static boolean writeConfigFile(String filePath, DnsFilter newConfig,String oldDomain) throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 直接使用传入的dnsFilter对象
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("dns-filter", lines, new DnsFilterUpdatePayload(newConfig, oldDomain));

            // 写入文件时保留原有格式
            Files.write(Paths.get(filePath), lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);

            return true;
        } catch (IOException e) {
            System.err.println("配置文件操作失败: " + e.getMessage());
            return false;
        }
    }

    public static boolean writeConfigFile(String filePath, DnsFilter config,boolean enable) throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 直接使用传入的dnsFilter对象
            ConfigUpdater configUpdater = new ConfigUpdater();
            String operationType = enable ? "dns-filter-enable" : "dns-filter-disable";

            List<String> updatedLines = configUpdater.updateConfig(
                    operationType,
                    lines,
                    new DnsFilterStatePayload(config.getDomainName(),enable)
            );

            Files.write(Paths.get(filePath), updatedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("配置文件操作失败: " + e.getMessage());
            return false;
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


    public static boolean saveConfigPortFile(String filePath, List<Interface> interfaces) throws IOException {
        try {
            // 读取配置文件所有行
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 创建配置更新器并指定策略类型为 "port"
            ConfigUpdater configUpdater = new ConfigUpdater();
            // 调用更新逻辑（传递 interfaces 作为配置数据）
            lines = configUpdater.updateConfig("port", lines, interfaces);

            // 写入更新后的配置
            Files.write(Paths.get(filePath),
                    lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("配置文件操作失败: " + e.getMessage());
            throw e; // 根据需求决定是否抛出异常
        } catch (Exception e) {
            System.err.println("配置更新逻辑错误: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveChronyConfigFile(String filePath, String instance) throws IOException {
        try {
            // 读取配置文件所有行
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 创建配置更新器并指定策略类型为 "chrony"
            ConfigUpdater configUpdater = new ConfigUpdater();
            // 调用更新逻辑（传递 interfaces 作为配置数据）
            lines = configUpdater.updateConfig("chrony-time", lines, instance);

            // 写入更新后的配置
            Files.write(Paths.get(filePath),
                    lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("配置文件操作失败: " + e.getMessage());
            throw e; // 根据需求决定是否抛出异常
        } catch (Exception e) {
            System.err.println("配置更新逻辑错误: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveNtpConfigFile(String filePath, List<String> instance) throws IOException {
        try {
            // 读取配置文件所有行
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 创建配置更新器并指定策略类型为 "ntp"
            ConfigUpdater configUpdater = new ConfigUpdater();
            // 调用更新逻辑
            lines = configUpdater.updateConfig("ntp", lines, instance);

            // 写入更新后的配置
            Files.write(Paths.get(filePath),
                    lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("配置文件操作失败: " + e.getMessage());
            throw e; // 根据需求决定是否抛出异常
        } catch (Exception e) {
            System.err.println("配置更新逻辑错误: " + e.getMessage());
            return false;
        }
    }

    public static boolean openNtpConfigFile(String filePath, Boolean instance) throws IOException {
        try {
            // 读取配置文件所有行
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            List<String> modifiedLines = new ArrayList<>();

            for (String line : lines) {
                String trimmed = line.trim();
                if (instance) {
                    // 当 instance=true 时：取消注释以 #allow 开头的行
                    if (trimmed.startsWith("#allow ")) {
                        modifiedLines.add(line.replaceFirst("#", "")); // 去掉第一个 # 符号
                    } else {
                        modifiedLines.add(line);
                    }
                } else {
                    // 当 instance=false 时：注释所有未注释的 allow 行
                    if (trimmed.startsWith("allow ")) {
                        modifiedLines.add(line.replaceFirst("allow", "#allow"));
                    } else {
                        modifiedLines.add(line);
                    }
                }
            }

            // 写入修改后的配置
            Files.write(
                    Paths.get(filePath),
                    modifiedLines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            return true;
        } catch (IOException e) {
            System.err.println("操作配置文件失败: " + e.getMessage());
            return false;
        }
    }


    public static Map<String, List<String>> selectChronyConfigFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        Map<String, List<String>> configMap = new HashMap<>();

        // 初始化池地址、允许的IP段和状态
        configMap.put("pool", new ArrayList<>());
        configMap.put("allow", new ArrayList<>());
        configMap.put("status2", new ArrayList<>()); // 新增状态字段

        boolean hasUncommentedAllow = false; // 标记是否存在未注释的 allow

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue; // 跳过注释和空行
            }

            // 提取 NTP 池地址
            if (trimmedLine.startsWith("pool ")) {
                String[] parts = trimmedLine.split("\\s+");
                if (parts.length >= 2) {
                    configMap.get("pool").add(parts[1]);
                }
            }

            // 提取 allow 后的 IP 段并检测是否未注释
            if (trimmedLine.startsWith("allow ")) {
                String[] parts = trimmedLine.split("\\s+");
                if (parts.length >= 2) {
                    configMap.get("allow").add(parts[1]);
                }
                hasUncommentedAllow = true; // 存在未注释的 allow 行
            }
        }

        // 设置状态：true 表示存在未注释的 allow，false 表示无
        configMap.get("status2").add(String.valueOf(hasUncommentedAllow));

        return configMap;
    }



    public static List<String> selectConfigPortFile(String filePath) throws IOException {
        List<String> interfaceNames = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean inServerBlock = false;

            for (String line : lines) {
                // 检测 server 块开始
                if (line.trim().startsWith("server:")) {
                    inServerBlock = true;
                    continue;
                }

                // 处理 server 块内内容
                if (inServerBlock) {
                    // 退出 server 块条件：遇到非缩进行（如顶级配置项）
                    if (!line.startsWith("    ") && !line.startsWith("\t") && !line.trim().isEmpty()) {
                        inServerBlock = false;
                        continue;
                    }

                    // 提取关键行：interface 或 do-ip6
                    if (line.trim().startsWith("interface:") || line.trim().startsWith("do-ip6:")) {
                        // 分割注释部分
                        String[] parts = line.split("#");
                        if (parts.length > 1) {
                            String comment = parts[1].trim();
                            // 处理逗号分隔的多个接口名
                            String[] names = comment.split(",\\s*");
                            for (String name : names) {
                                if (!name.isEmpty()) {
                                    interfaceNames.add(name);
                                }
                            }
                        }
                    }
                }
            }

            // 去重后返回
            return interfaceNames.stream()
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("配置文件读取失败: " + e.getMessage());
            throw e;
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

    public static boolean updateInterfaceFile(String filePath, Unbound unbound) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 解析数据
            Set<String> forwardAddress = unbound.getInterfaces();
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 更新配置
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("interface", lines, forwardAddress);
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

    public static boolean deleteConfigFile(String filePath, Set<String> domain) throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 直接使用传入的dnsFilter对象
            ConfigUpdater configUpdater = new ConfigUpdater();
            lines = configUpdater.updateConfig("dns-filter-remove", lines, domain);

            // 写入文件时保留原有格式
            Files.write(Paths.get(filePath), lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);

            return true;
        } catch (IOException e) {
            System.err.println("配置文件操作失败: " + e.getMessage());
            return false;
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
