package com.metoo.nrsm.core.config.utils.gather.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 23:07
 */
public class Sshtest {

    public static void main(String[] args) {
        String[] params4 = {"huawei", "192.168.100.1", "ssh", "22", "metoo", "metoo89745000",
                "20", "1"};

        String result = exec("/opt/netmap/script/traffic.py", params4);

        insertTraffic2(result);

    }

    public static Integer ipv4InboundResult(String data, String rule) {
        if (StringUtil.isNotEmpty(data)) {
            JSONArray jsonArray = JSONArray.parseArray(data);
            if (jsonArray.size() > 0) {

                int ipv4Inbound1 = 0;
                int ipv4Inbound2 = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.get("Type").equals("sum-ipv4, inbound")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Inbound1 += Integer.parseInt(nestedObject.getString(rule));
                                    ipv4Inbound2 += Integer.parseInt(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                    int ipv4Outbound1 = 0;
                    int ipv4Outbound2 = 0;

                    if (jsonObject.get("Type").equals("sum-ipv4, outbound")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Outbound1 += Integer.parseInt(nestedObject.getString(rule));
                                    ipv4Outbound2 += Integer.parseInt(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }


                    int ipv6Inbound1 = 0;
                    int ipv6Inbound2 = 0;

                    if (jsonObject.get("Type").equals("sum-ipv6, inbound")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Outbound1 += Integer.parseInt(nestedObject.getString(rule));
                                    ipv4Outbound2 += Integer.parseInt(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                    int ipv6Outbound1 = 0;
                    int ipv6Outbound2 = 0;

                    if (jsonObject.get("Type").equals("sum-ipv6, outbound")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Outbound1 += Integer.parseInt(nestedObject.getString(rule));
                                    ipv4Outbound2 += Integer.parseInt(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                }
                System.out.println("ipv4Inbound1: " + ipv4Inbound1);
                System.out.println("ipv4Inbound2: " + ipv4Inbound2);
                return ipv4Inbound1 - ipv4Inbound2;
            }
        }
        return 0;
    }

    public static void insertTraffic2(String data) {
        if (StringUtil.isNotEmpty(data)) {
            JSONArray jsonArray = JSONArray.parseArray(data);
            if (jsonArray.size() > 0) {

                int ipv4Inbound = 0;

                int ipv4Outbound = 0;

                int ipv6Inbound = 0;

                int ipv6Outbound = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.get("Protocol").equals("Ipv4")) {
                        if (jsonObject.containsKey("Input")) {
                            ipv4Inbound += Integer.parseInt(jsonObject.getString("Input"));
                        }
                        if (jsonObject.containsKey("Output")) {
                            ipv4Outbound += Integer.parseInt(jsonObject.getString("Output"));
                        }
                    }

                    if (jsonObject.get("Protocol").equals("Ipv6")) {
                        if (jsonObject.containsKey("Input")) {
                            ipv6Inbound += Integer.parseInt(jsonObject.getString("Input"));
                        }
                        if (jsonObject.containsKey("Output")) {
                            ipv6Outbound += Integer.parseInt(jsonObject.getString("Output"));
                        }
                    }
                }


                Integer vfourFlow = (ipv4Inbound + ipv4Outbound) / 1000;

                DecimalFormat df = new DecimalFormat("#.##");
                String formattedVfourFlow = df.format(vfourFlow);

                Integer vsixFlow = (ipv6Inbound + ipv6Outbound) / 1000;
                String formattedVsixFlow = df.format(vsixFlow);

                System.out.println(formattedVfourFlow);
                System.out.println(formattedVsixFlow);
            }
        }
    }

    public static String exec(String path, String[] params) {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";

        Session session = null;

        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        try {
            conn.connect();
            // 验证用户密码
            conn.authenticateWithPassword(username, password);
            session = conn.openSession();
            String py_version = "python3";

            String[] args = new String[]{
                    py_version, path};

            if (params.length > 0) {
                String[] mergedArray = new String[args.length + params.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = params[i - argsLen];
                    }
                }
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String str : mergedArray) {
                        sb.append(str).append(" ");
                    }
                    session.execCommand(sb.toString().trim());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String str : args) {
                        sb.append(str).append(" ");
                    }
                    session.execCommand(sb.toString().trim());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 消费所有输入流
            String inStr = consumeInputStream(session.getStdout());
            return inStr;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return "";
    }

    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }


    @Test
    public void test() throws IOException {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";
        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);

        Session session = conn.openSession();

        session.execCommand("python3 /opt/nrsm/py/gethostname.py 1.2.3.9 v2c public@123");

        // 消费所有输入流
        String inStr = consumeInputStream(session.getStdout());
        String errStr = consumeInputStream(session.getStderr());

        System.out.println(inStr);
        if (StringUtils.isNotEmpty(inStr)) {
            System.out.println(1);
        } else {
            System.out.println(2);
        }

        session.close();
        conn.close();
    }
}
