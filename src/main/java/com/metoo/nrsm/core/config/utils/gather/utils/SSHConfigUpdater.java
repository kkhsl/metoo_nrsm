package com.metoo.nrsm.core.config.utils.gather.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SSHConfigUpdater {
    public void test1() throws Exception {
        String host = "192.168.6.101";
        String username = "metoo";
        String password = "metoo89745000";
        int port = 22;

        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        conn.connect();
        // 验证用户密码
        conn.authenticateWithPassword(username, password);

        // 发送 JSON 数据
        String jsonPayload = "{\n" +
                "    \"privateAddress\": 1\n" +
                "}";

        // 通过 HTTP 请求修改
        String url = "http://localhost:8960/nrsm/admin/unbound/open";
        String url2 = "http://localhost:8960/nrsm/buyer/login?username=admin&password=123456&captcha=2ydu";
        sendHttpGet(url2);
        sendHttpPost(url, jsonPayload);

        Session session = conn.openSession();
        session.execCommand("systemctl restart unbound");
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        System.out.println("Unbound 状态:\n" + statusOutput);
        session.close(); // 关闭会话

        // 检查 Unbound 服务状态
        boolean isRunning = checkUnboundStatus(conn);
        if (isRunning) {
            System.out.println("Unbound 服务已启动。");
        } else {
            System.out.println("Unbound 服务未启动，请检查。");
        }
        // 关闭连接
        conn.close();
    }


    private boolean checkUnboundStatus(Connection conn) throws Exception {
        Session session = conn.openSession();
        session.execCommand("systemctl status unbound");
        String statusOutput = consumeInputStream(session.getStdout());
        session.close(); // 关闭会话
        // 判断服务状态
        return statusOutput.contains("Active: active (running)");
    }


    private void sendHttpPost(String url, String jsonContent) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json"); // 设置请求头为 JSON 类型
            post.setEntity(new StringEntity(jsonContent)); // 将 JSON 字符串作为请求体

            // 执行请求
            HttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            System.out.println("HTTP 响应: " + responseString); // 打印响应内容
        }
    }

    private String consumeInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private String sendHttpGet(String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);

            // 执行 GET 请求
            HttpResponse response = httpClient.execute(get);
            String responseString = EntityUtils.toString(response.getEntity()); // 获取响应内容

            // 输出返回结果
            System.out.println("GET 请求响应:\n" + responseString);

            return responseString; // 返回响应内容
        }
    }

    public static void main(String[] args) {
        SSHConfigUpdater updater = new SSHConfigUpdater();
        try {
            updater.test1();
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
        }
    }
}