package com.metoo.nrsm.core.system.conf.network.strategy;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.entity.Interface;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NetplanConfigManager {

//    private static final String CONFIG_PATH = "/etc/netplan/00-installer-config.yaml";
//    private static final String BACKUP_DIR = "/etc/netplan/backups/";

    private static final String CONFIG_PATH = "/etc/netplan/00-installer-config.yaml";
    private static final String BACKUP_DIR = "/etc/netplan/backups/";


//    private static final String CONFIG_PATH = "C:\\Users\\leo\\Desktop\\update\\00-installer-config.yaml";
//    private static final String BACKUP_DIR = "C:\\Users\\leo\\Desktop\\update";

    private static final DateTimeFormatter BACKUP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 备份当前配置文件
     */
    public static void backupCurrentConfig() throws Exception {
        // 创建备份目录
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // 生成备份文件名
        String backupFileName = "backup_" + LocalDateTime.now().format(BACKUP_FORMAT) + ".yaml";
        Path backupPath = Paths.get(BACKUP_DIR + backupFileName);

        // 复制文件
        Files.copy(Paths.get(CONFIG_PATH), backupPath);
    }

    /**
     * 更新接口配置
     */
    public static void updateInterfaceConfig(Interface iface) throws Exception {
        // 备份当前配置
        backupCurrentConfig();

        // 加载当前YAML配置
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            config = yaml.load(fis);
        }

        // 获取网络配置部分
        Map<String, Object> network = (Map<String, Object>) config.get("network");
        if (iface.getVlanNum() != null) {
            updateVlanConfig(network, iface);
        } else {
            String interfaceType = determineInterfaceType(network, iface.getName());

            if ("bridge".equals(interfaceType)) {
                updateBridgeConfig(network, iface);
            } else {
                updateEthernetConfig(network, iface);
            }
        }

        try {
            // 保存修改后的配置
            saveConfig(config);

            // 应用配置
            applyNetplanConfig();
        } catch (Exception e) {
            e.printStackTrace();
            restoreConfig();
        }
    }

    /**
     * 根据接口名称在配置中的位置判断接口类型
     */
    private static String determineInterfaceType(Map<String, Object> network, String interfaceName) {
        // 检查网桥部分
        Map<String, Object> bridges = (Map<String, Object>) network.get("bridges");
        if (bridges != null && bridges.containsKey(interfaceName)) {
            return "bridge";
        }

        // 检查VLAN部分
        Map<String, Object> vlans = (Map<String, Object>) network.get("vlans");
        if (vlans != null && vlans.containsKey(interfaceName)) {
            return "vlan";
        }

        // 默认为以太网接口
        return "ethernet";
    }

    private static void updateBridgeConfig(Map<String, Object> network, Interface iface) {
        Map<String, Object> bridges = getOrCreateSection(network, "bridges");

        Map<String, Object> bridgeConfig;
        if (bridges.containsKey(iface.getName())) {
            bridgeConfig = (Map<String, Object>) bridges.get(iface.getName());
        } else {
            bridgeConfig = new LinkedHashMap<>();
        }

        if (!bridgeConfig.containsKey("interfaces") && bridgeConfig.containsKey("interfaces")) {

        }

        if (iface.getIpv4Address() == null && iface.getIpv6Address() == null) {
            bridgeConfig.put("dhcp4", true);
            bridgeConfig.put("dhcp6", true);
        } else {
            bridgeConfig.put("dhcp4", false);
            bridgeConfig.put("dhcp6", false);
        }

        if (iface.getIpv4Address() != null || iface.getIpv6Address() != null) {
            List<String> addresses = new ArrayList<>();
            if (iface.getIpv4Address() != null) addresses.add(iface.getIpv4Address());
            if (iface.getIpv6Address() != null) addresses.add(iface.getIpv6Address());
            bridgeConfig.put("addresses", addresses);
        } else {
            bridgeConfig.remove("addresses");
        }

        if (iface.getGateway4() != null) {
            bridgeConfig.put("gateway4", iface.getGateway4());
        } else {
            bridgeConfig.remove("gateway4");
        }

        if (iface.getGateway6() != null) {
            bridgeConfig.put("gateway6", iface.getGateway6());
        } else {
            bridgeConfig.remove("gateway6");
        }

        if (!bridgeConfig.containsKey("nameservers") && bridgeConfig.containsKey("nameservers")) {

        }

        if (!bridgeConfig.containsKey("parameters") && bridgeConfig.containsKey("parameters")) {

        }

        bridges.put(iface.getName(), bridgeConfig);
    }

    private static Map<String, Object> getOrCreateSection(Map<String, Object> network, String sectionName) {
        Map<String, Object> section = (Map<String, Object>) network.get(sectionName);
        if (section == null) {
            section = new LinkedHashMap<>();
            network.put(sectionName, section);
        }
        return section;
    }

    private static Map<String, Object> createBaseInterfaceConfig(Interface iface) {
        Map<String, Object> config = new LinkedHashMap<>();

        if (iface.getIpv4Address() == null && iface.getIpv6Address() == null) {
            config.put("dhcp4", true);
            config.put("dhcp6", true);
        } else {
            config.put("dhcp4", false);
            config.put("dhcp6", false);

            List<String> addresses = new ArrayList<>();
            if (iface.getIpv4Address() != null) addresses.add(iface.getIpv4Address());
            if (iface.getIpv6Address() != null) addresses.add(iface.getIpv6Address());
            config.put("addresses", addresses);

            if (iface.getGateway4() != null) config.put("gateway4", iface.getGateway4());
            if (iface.getGateway6() != null) config.put("gateway6", iface.getGateway6());
        }
        return config;
    }


    /**
     * 更新以太网接口配置
     */
    private static void updateEthernetConfig(Map<String, Object> network, Interface iface) {
        // 获取或创建ethernets部分
        Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
        if (ethernets == null) {
            ethernets = new LinkedHashMap<>();
            network.put("ethernets", ethernets);
        }

        // 创建或更新接口配置
        Map<String, Object> ifaceConfig = new LinkedHashMap<>();
        if (iface.getIpv4Address() == null && iface.getIpv6Address() == null) {
            // 使用DHCP
            ifaceConfig.put("dhcp4", true);
            ifaceConfig.put("dhcp6", true);
        } else {
            // 静态IP配置
            ifaceConfig.put("dhcp4", false);
            ifaceConfig.put("dhcp6", false);

            // 添加IP地址
            List<String> addresses = new java.util.ArrayList<>();
            if (iface.getIpv4Address() != null) {
                addresses.add(iface.getIpv4Address());
            }
            if (iface.getIpv6Address() != null) {
                addresses.add(iface.getIpv6Address());
            }
            if (!addresses.isEmpty()) {
                ifaceConfig.put("addresses", addresses);
            }

            // 添加网关
            if (iface.getGateway4() != null && StringUtil.isNotEmpty(iface.getGateway4())) {
                ifaceConfig.put("gateway4", iface.getGateway4());
            }
            // 添加IPv6网关
            if (iface.getGateway6() != null && StringUtil.isNotEmpty(iface.getGateway6())) {
                ifaceConfig.put("gateway6", iface.getGateway6());
            }
        }

        // 更新接口配置
        ethernets.put(iface.getName(), ifaceConfig);
    }

    /**
     * 更新VLAN接口配置
     */
    private static void updateVlanConfig(Map<String, Object> network, Interface iface) {
        // 获取或创建vlans部分
        Map<String, Object> vlans = (Map<String, Object>) network.get("vlans");
        if (vlans == null) {
            vlans = new LinkedHashMap<>();
            network.put("vlans", vlans);
        }

        // 1. 首先确保主接口的DHCP被禁用（如果VLAN配置了静态IP）
        if ((iface.getIpv4Address() != null || iface.getIpv6Address() != null) &&
                iface.getParentName() != null) {

            Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
            if (ethernets != null) {
                Map<String, Object> parentConfig = (Map<String, Object>) ethernets.get(iface.getParentName());
                if (parentConfig != null) {
                    // 禁用主接口的DHCP
                    parentConfig.put("dhcp4", false);
                    parentConfig.put("dhcp6", false);
                }
            }
        }

        // 创建或更新VLAN配置
        Map<String, Object> vlanConfig = new LinkedHashMap<>();
        vlanConfig.put("id", iface.getVlanNum());
        vlanConfig.put("link", iface.getParentName());

        // 添加IP地址
        List<String> addresses = new java.util.ArrayList<>();
        if (iface.getIpv4Address() != null) {
            addresses.add(iface.getIpv4Address());
        }
        if (iface.getIpv6Address() != null) {
            addresses.add(iface.getIpv6Address());
        }
        if (!addresses.isEmpty()) {
            vlanConfig.put("addresses", addresses);
        }

        // 添加路由（支持IPv4和IPv6网关）
        List<Map<String, String>> routes = new ArrayList<>();

        // IPv4网关
        if (iface.getGateway4() != null && StringUtil.isNotEmpty(iface.getGateway4())) {
            Map<String, String> ipv4Route = new LinkedHashMap<>();
            ipv4Route.put("to", "0.0.0.0/0");
            ipv4Route.put("via", iface.getGateway4());
            routes.add(ipv4Route);
        }

        // IPv6网关
        if (iface.getGateway6() != null && StringUtil.isNotEmpty(iface.getGateway6())) {
            Map<String, String> ipv6Route = new LinkedHashMap<>();
            ipv6Route.put("to", "::/0");
            ipv6Route.put("via", iface.getGateway6());
            routes.add(ipv6Route);
        }

        if (!routes.isEmpty()) {
            vlanConfig.put("routes", routes);
        }

        // 更新VLAN配置
        vlans.put(iface.getParentName() + "." + iface.getVlanNum(), vlanConfig);
    }

    /**
     * 保存配置到文件
     */
    private static void saveConfig(Map<String, Object> config) throws Exception {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            yaml.dump(config, writer);
        }
    }

    /**
     * 应用netplan配置
     */
    private static void applyNetplanConfig() throws Exception {
        Process process = Runtime.getRuntime().exec("netplan apply");
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to apply netplan configuration");
        }
    }

    /**
     * 恢复备份的配置
     */
    public static void restoreConfig() throws Exception {
        // 查找最新的备份文件
        File backupDir = new File(BACKUP_DIR);
        File[] backupFiles = backupDir.listFiles();

        if (backupFiles == null || backupFiles.length == 0) {
            throw new RuntimeException("No backup files found");
        }

        // 按修改时间排序，获取最新的备份
        File latestBackup = backupFiles[0];
        for (File file : backupFiles) {
            if (file.lastModified() > latestBackup.lastModified()) {
                latestBackup = file;
            }
        }

        // 复制备份文件到配置位置
        Files.copy(latestBackup.toPath(), Paths.get(CONFIG_PATH));

        // 应用配置
        applyNetplanConfig();
    }


    /**
     * 删除指定的VLAN接口配置
     *
     * @param vlanInterfaceName 要删除的VLAN接口名称（如"enp2s0f1.200"）
     */
    public static void removeVlanInterface(String vlanInterfaceName) throws Exception {
        // 备份当前配置
        backupCurrentConfig();

        // 加载当前YAML配置
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            config = yaml.load(fis);
        }

        // 获取网络配置部分
        Map<String, Object> network = (Map<String, Object>) config.get("network");
        if (network == null) {
            throw new RuntimeException("No network configuration found");
        }

        // 获取vlans部分
        Map<String, Object> vlans = (Map<String, Object>) network.get("vlans");
        if (vlans == null || vlans.isEmpty()) {
            throw new RuntimeException("No VLAN configurations found");
        }

        // 检查并删除指定的VLAN接口
        if (vlans.containsKey(vlanInterfaceName)) {
            vlans.remove(vlanInterfaceName);

            // 如果vlans部分为空，则移除整个vlans部分
            if (vlans.isEmpty()) {
                network.remove("vlans");
            }

            // 保存修改后的配置
            saveConfig(config);

            // 应用配置
            applyNetplanConfig();
        } else {
            throw new RuntimeException("VLAN interface " + vlanInterfaceName + " not found");
        }
    }

    public static void removeBridgeInterface(String bridgeName) throws Exception {
        backupCurrentConfig();
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            config = yaml.load(fis);
        }

        Map<String, Object> network = (Map<String, Object>) config.get("network");
        Map<String, Object> bridges = (Map<String, Object>) network.get("bridges");

        if (bridges != null && bridges.containsKey(bridgeName)) {
            bridges.remove(bridgeName);
            if (bridges.isEmpty()) network.remove("bridges");

            saveConfig(config);
            applyNetplanConfig();
        } else {
            throw new RuntimeException("Bridge interface " + bridgeName + " not found");
        }
    }

}
