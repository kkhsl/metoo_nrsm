package com.metoo.nrsm.core.utils.unbound;

import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UnboundConf {

    private static final String CONFIG_FILE = "C:\\language\\java\\project\\metoo\\nrsm\\metoo_nrsm\\src\\main\\resources\\dns\\unbound.conf";


    public static void main(String[] args) throws IOException {
        String zoneName = "szzs.com.";
        String zoneType = "static";
        String hostName = "www.szzs.com.";
        String recordType = "A";
        String mappedAddress = "192.168.5.206";
        boolean privateAddress = false; // 控制 private-address 是否被注释

        // 动态添加或更新local-zone和local-data，同时处理private-address
        addOrUpdateLocalZoneData(zoneName, zoneType, hostName, recordType, mappedAddress, privateAddress);
    }

    public static void addOrUpdateLocalZoneData(String zoneName, String zoneType,
                                                String hostName, String recordType,
                                                String mappedAddress, boolean privateAddress) throws IOException {
        // 读取文件内容
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        // 查找 server 块的末尾
        int serverEndIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("remote-control:")) {
                serverEndIndex = i - 1;
                break;
            }
        }

        if (serverEndIndex == -1) {
            throw new IllegalStateException("未找到 server 块的末尾");
        }

        // 检查是否已经存在对应的local-zone或local-data
        boolean zoneExists = false;
        boolean dataExists = false;
        for (int i = 0; i <= serverEndIndex; i++) {
            String line = lines.get(i).trim();
            if (line.equals(String.format("local-zone: \"%s\" %s", zoneName, zoneType))) {
                zoneExists = true;
            }
            if (line.equals(String.format("local-data: \"%s IN %s %s\"", hostName, recordType, mappedAddress))) {
                dataExists = true;
            }
        }

        // 添加local-zone
        if (!zoneExists) {
            lines.add(serverEndIndex + 1, String.format("local-zone: \"%s\" %s", zoneName, zoneType));
            serverEndIndex++;
        }

        // 添加local-data
        if (!dataExists) {
            lines.add(serverEndIndex + 1, String.format("local-data: \"%s IN %s %s\"", hostName, recordType, mappedAddress));
        }

        // 处理 private-address: ::/0 的注释
        boolean privateAddressProcessed = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("private-address: ::/0")) {
                if (privateAddress && !line.startsWith("#")) {
                    // 如果需要注释，且当前行未注释
                    lines.set(i, "# " + lines.get(i));
                } else if (!privateAddress && line.startsWith("#")) {
                    // 如果需要取消注释，且当前行已注释
                    lines.set(i, lines.get(i).substring(2));
                }
                privateAddressProcessed = true;
                break;
            }
        }

        // 如果未找到 private-address，则在 server 块中添加
        if (!privateAddressProcessed && privateAddress) {
            lines.add(serverEndIndex + 1, "# private-address: ::/0");
        } else if (!privateAddressProcessed) {
            lines.add(serverEndIndex + 1, "private-address: ::/0");
        }

        // 写回文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }

        System.out.println("配置文件更新完成");
    }
}
