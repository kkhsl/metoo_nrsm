package com.metoo.nrsm.core.network.networkconfig.test;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class modifyIp {

    private static final String NETPLAN_PATH = "/etc/netplan/00-installer-config.yaml";
//    private static final String NETPLAN_PATH = "C:\\Users\\leo\\Desktop\\00-installer-config.yaml";


    /**
     * modifyip.py
     *
     * @param iface
     * @param ipv4address
     * @param ipv6address
     * @param gateway4
     * @param gateway6
     * @return
     * @throws Exception
     */
    public static int modifyIP(String iface, String ipv4address, String ipv6address,
                               String gateway4, String gateway6) throws Exception {
        // 1. 加载并备份原始配置
        Yaml yaml = new Yaml();
        Map<String, Object> originalConfig;
        try (InputStream input = Files.newInputStream(Paths.get(NETPLAN_PATH))) {
            originalConfig = yaml.load(input);
        }

        // 深度拷贝备份
        Map<String, Object> backupConfig = deepCopy(originalConfig);

        // 2. 修改配置结构
        try {
            modifyInterfaceConfig(originalConfig, iface, ipv4address, ipv6address, gateway4, gateway6);
            writeYamlConfig(originalConfig);
        } catch (Exception e) {
            restoreConfig(backupConfig);
            throw new IOException("配置修改失败，已回滚", e);
        }

        // 3. 应用新配置
        int status = executeNetplanApply();
        if (status != 0) {
            restoreConfig(backupConfig);
        }
        return status;
    }

    @SuppressWarnings("unchecked")
    private static void modifyInterfaceConfig(Map<String, Object> config, String iface,
                                              String ipv4, String ipv6,
                                              String gw4, String gw6) {
        Map<String, Object> network = (Map<String, Object>) config.get("network");
        Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");

        // 初始化接口配置
        if (!ethernets.containsKey(iface) || ethernets.get(iface) == null) {
            ethernets.put(iface, new HashMap<>());
        }

        Map<String, Object> ifaceConfig = (Map<String, Object>) ethernets.get(iface);
        List<String> addresses = new ArrayList<>();

        // 处理IPv4
        if (!ipv4.isEmpty()) {
            addresses.add(ipv4);
        }

        // 处理IPv6
        if (!ipv6.isEmpty()) {
            addresses.add(ipv6);
        }

        // 更新地址列表
        if (!addresses.isEmpty()) {
            ifaceConfig.put("addresses", addresses);
        } else {
            ifaceConfig.remove("addresses");
        }

        // 更新网关
        updateGateway(ifaceConfig, "gateway4", gw4);
        updateGateway(ifaceConfig, "gateway6", gw6);
    }

    private static void updateGateway(Map<String, Object> config, String key, String value) {
        if (!value.isEmpty()) {
            config.put(key, value);
        } else {
            config.remove(key);
        }
    }

    private static void writeYamlConfig(Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);

        // 自定义Representer处理空集合
        Representer representer = new Representer(options) {
            protected Node representSequence(Tag tag, List<?> sequence, Boolean flowStyle) {
                // 对空列表强制使用流样式 []
                if (sequence.isEmpty()) {
                    flowStyle = DumperOptions.FlowStyle.FLOW.getStyleBoolean();
                }
                return super.representSequence(tag, sequence, DumperOptions.FlowStyle.fromBoolean(flowStyle));
            }
        };

        Yaml yaml = new Yaml(representer, options);
        try (Writer writer = Files.newBufferedWriter(Paths.get(NETPLAN_PATH))) {
            yaml.dump(config, writer);
        }
    }

    private static int executeNetplanApply() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("netplan", "apply");
        Process p = pb.start();
        return p.waitFor();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<String, Object> original) {
        Yaml yaml = new Yaml();
        return yaml.load(yaml.dump(original));
    }

    private static void restoreConfig(Map<String, Object> backup) throws IOException {
        writeYamlConfig(backup);
    }

    public static void main(String[] args) {
        try {
            int result = modifyIP("ens160",
                    "192.168.60.90/24",
                    "2401:C1:9006::AC10:FDF2/126",
                    "192.168.60.1",
                    "2401:C1:9006::AC10:FDF1");

            System.out.println("操作结果: " + (result == 0 ? "成功" : "失败"));
        } catch (Exception e) {
            System.err.println("配置错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}