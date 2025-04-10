package com.metoo.nrsm.core.network.networkconfig.other;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.mapper.PortIpv6Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.*;

@RequestMapping("/admin/subnetipv6")
@RestController
public class Subnetipv6 {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PortIpv6Mapper portIpv6Mapper;

    // IPv6 工具类
    static class IPv6Utils {
        public static String calculateNetwork(String address, int prefix) {
            try {
                Inet6Address ip = (Inet6Address) InetAddress.getByName(address.split("/")[0]);
                byte[] bytes = ip.getAddress();
                applyPrefixMask(bytes, prefix);
                return formatIPv6(bytes) + "/" + prefix;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid IPv6: " + address);
            }
        }

        private static void applyPrefixMask(byte[] bytes, int prefix) {
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;

            // 处理完整字节
            for (int i = fullBytes + 1; i < 16; i++) {
                bytes[i] = 0;
            }

            // 处理剩余位
            if (remainingBits > 0 && fullBytes < 15) {
                int mask = 0xFF << (8 - remainingBits);
                bytes[fullBytes] = (byte) (bytes[fullBytes] & mask);
            }
        }

        private static String formatIPv6(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                if (i > 0) sb.append(":");
                sb.append(String.format("%02x%02x", bytes[i], bytes[i + 1]));
            }
            return sb.toString()
                    .replaceAll("(:0){2,}", "::")
                    .replaceAll("^0::", "::");
        }
    }

    @RequestMapping("/list")
    public String analyzeSubnets() {
        List<String> ipv6List = portIpv6Mapper.selectIpv6Cidrs();
        Map<String, List<String>> subnet64 = new LinkedHashMap<>();
        Map<String, List<String>> subnet56 = new LinkedHashMap<>();

        // 生成去重网络列表
        Set<String> networks = new LinkedHashSet<>();
        for (String ip : ipv6List) {
            String[] parts = ip.split("/");
            networks.add(IPv6Utils.calculateNetwork(parts[0], Integer.parseInt(parts[1])));
        }

        // 构建64位子网结构
        for (String network : networks) {
            String[] parts = network.split("/");
            int prefix = Integer.parseInt(parts[1]);
            String base64 = IPv6Utils.calculateNetwork(parts[0], 64);

            if (prefix > 64) {
                subnet64.computeIfAbsent(base64, k -> new ArrayList<>()).add(network);
            } else {
                subnet64.putIfAbsent(base64, new ArrayList<>());
            }
        }

        // 构建56位父网络
        for (String network64 : subnet64.keySet()) {
            String[] parts = network64.split("/");
            String base56 = IPv6Utils.calculateNetwork(parts[0], 56);
            subnet56.computeIfAbsent(base56, k -> new ArrayList<>()).add(network64);
        }

        // 生成嵌套结构
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry56 : subnet56.entrySet()) {
            Map<String, List<String>> children = new LinkedHashMap<>();
            for (String network64 : entry56.getValue()) {
                children.put(network64, subnet64.get(network64));
            }
            result.put(entry56.getKey(), children);
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("JSON生成失败", e);
        }
    }

    /*public static void main(String[] args) {
        System.out.println(analyzeSubnets());
    }*/

}