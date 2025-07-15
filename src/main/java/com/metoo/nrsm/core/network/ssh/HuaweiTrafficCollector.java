package com.metoo.nrsm.core.network.ssh;

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HuaweiTrafficCollector {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("brand", "huawei");
        params.put("host", "192.168.6.101");
        params.put("method", "ssh");
        params.put("port", "22");
        params.put("username", "root");
        params.put("password", "Metoo89745000!");
        params.put("vlan", "4004");
        params.put("type", "0");

        try {
            if ("huawei".equals(params.get("brand"))) {
                Session session = connectDevice(params);
                String direction = parseDirection(params.get("type")); // 新增方向解析
                System.out.println(getTraffic(session, params.get("vlan"), direction));
                session.disconnect();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(2);
        }
    }

    // 新增方向解析方法
    private static String parseDirection(String type) {
        switch (type) {
            case "0":
                return "outbound";
            case "1":
                return "inbound";
            default:
                throw new IllegalArgumentException("无效的类型参数: " + type);
        }
    }

    // SSH/Telnet连接实现
    private static Session connectDevice(Map<String, String> params) throws JSchException {
        JSch jsch = new JSch();
        String method = "telnet".equals(params.get("method")) ?
                "telnet" : "ssh";
        Session session = jsch.getSession(params.get("username"), params.get("host"), Integer.parseInt(params.get("port")));
        session.setPassword(params.get("password"));
        session.setConfig("StrictHostKeyChecking", "no");

        // Telnet配置
        if ("telnet".equals(method)) {
            session.setConfig("protocol", "telnet");
            session.setConfig("PreferredAuthentications", "password");
        }

        session.connect(15000); // 15秒超时
        return session;
    }

    // 实现get_traffic
    private static String getTraffic(Session session, String vlan, String direction) throws Exception {
        ChannelExec channel = null;
        String command = "";
        channel = (ChannelExec) session.openChannel("exec");


        // 根据方向参数构建命令
        // String command = String.format("dis controller-policy statistics interface vlanif %s %s rule-base\n", vlan, direction);
        command = String.format("cat test_out.txt");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        // 获取命令输出
        InputStream in = channel.getInputStream();
        channel.connect();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return parseTrafficData(output.toString(), direction);
        } catch (Exception e) {
            System.err.println("Error during remote connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭频道和会话
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return null;
    }

    private static String parseTrafficData(String input, String direction) {
        List<Map<String, Object>> policies = new ArrayList<>();

        // 使用更精确的切割方式
        String[] policyBlocks = input.split("(?=Traffic policy: )");

        Pattern policyPattern = Pattern.compile(
                "Traffic policy: ([^,]+?),\\s*" + Pattern.quote(direction)
        );

        for (String block : policyBlocks) {
            Matcher policyMatcher = policyPattern.matcher(block);
            if (policyMatcher.find()) {
                Map<String, Object> policy = new LinkedHashMap<>();
                policy.put("policy_name", policyMatcher.group(1));
                policy.put("direction", direction);

                // 解析槽位数据
                Map<String, Map<String, String>> slots = new LinkedHashMap<>();
                Matcher slotMatcher = Pattern.compile(
                        "Slot:\\s+(\\S+)(.*?)(?=(Slot:\\s|Traffic policy:|$))",
                        Pattern.DOTALL
                ).matcher(block);

                while (slotMatcher.find()) {
                    String slotId = slotMatcher.group(1);
                    String slotContent = slotMatcher.group(2);
                    slots.put(slotId, parseSlotRules(slotContent));
                }

                policy.put("slots", slots);
                policies.add(policy);
            }
        }

        return gson.toJson(policies);
    }

    private static Map<String, String> parseSlotRules(String content) {
        Map<String, String> rules = new LinkedHashMap<>();
        Matcher ruleMatcher = Pattern.compile(
                "rule\\s+(\\d+).*?Passed\\s+bps\\s+(\\d+)",
                Pattern.DOTALL
        ).matcher(content);

        while (ruleMatcher.find()) {
            rules.put(ruleMatcher.group(1), ruleMatcher.group(2));
        }
        return rules;
    }


    // 辅助方法：统计关键词出现次数
    private static int countOccurrences(String source, String target) {
        return source.split(target, -1).length - 1;
    }

    // 解析槽位数据
    private static void processSlots(Map<String, Object> policy, String content) {
        int slotCount = countOccurrences(content, "Slot:");
        int pos = -1;

        for (int j = 0; j < slotCount; j++) {
            pos = content.indexOf("Slot:", pos + 1);
            String slotId = content.substring(pos + 5)
                    .split("\\s", 2)[0].trim();

            Map<String, String> rules = new HashMap<>();
            String slotContent = (j != slotCount - 1) ?
                    content.substring(pos, content.indexOf("Slot:", pos + 1)) :
                    content.substring(pos);

            processRules(rules, slotContent);
            policy.put(slotId, rules);
        }
    }

    // 解析规则数据
    private static void processRules(Map<String, String> rules, String content) {
        Pattern rulePattern = Pattern.compile("rule (\\d+)");
        Matcher matcher = rulePattern.matcher(content);

        while (matcher.find()) {
            String ruleId = matcher.group(1);
            int bpsPos = content.indexOf("Passed bps", matcher.end());
            String bps = content.substring(bpsPos + 10)
                    .split("\\s", 2)[0].trim();
            rules.put(ruleId, bps);
        }
    }


}