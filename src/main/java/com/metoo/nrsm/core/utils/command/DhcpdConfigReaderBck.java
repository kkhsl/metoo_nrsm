package com.metoo.nrsm.core.utils.command;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class DhcpdConfigReaderBck {

    private static final String REMOTE_FILE_PATH = "/etc/dhcp/dhcpd.conf";
    private static final String LOCAL_TEMP_PATH = "./temp_dhcpd.conf";

    /**
     * 读取 dhcpd.conf 文件
     *
     * @param mode     模式 ("dev" 或 "prob")
     * @param host     远程主机地址 (仅 dev 模式需要)
     * @param username 远程主机用户名 (仅 dev 模式需要)
     * @param password 远程主机密码 (仅 dev 模式需要)
     * @throws Exception 读取或文件传输失败时抛出
     */
    public void readDhcpdConfig(String mode, String host, Integer port, String username, String password) throws Exception {
        if ("dev".equalsIgnoreCase(mode)) {
            readFromRemote(host, port, username, password);
        } else if ("prob".equalsIgnoreCase(mode)) {
            readFromLocalAfterUpload();
        } else {
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }

    /**
     * 从远程服务器读取 dhcpd.conf 文件
     *
     * @param host     远程主机地址
     * @param username 远程主机用户名
     * @param password 远程主机密码
     * @throws Exception 文件传输或读取失败时抛出
     */
    private InputStream readFromRemote(String host, Integer port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            InputStream inputStream = channelSftp.get(REMOTE_FILE_PATH);
            String content = readFileContent(inputStream);

            System.out.println("Content from remote:");

            System.out.println(content);

        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return null;
    }

    /**
     * 上传后从本地读取 dhcpd.conf 文件
     *
     * @throws IOException 本地文件读取失败时抛出
     */
//    private void readFromLocalAfterUpload() throws IOException {
//        Path path = Paths.get(LOCAL_TEMP_PATH);
//        if (Files.exists(path)) {
//            String content = Files.readString(path);
//            System.out.println("Content from local:");
//            System.out.println(content);
//        } else {
//            throw new FileNotFoundException("Local temp file not found: " + LOCAL_TEMP_PATH);
//        }
//    }
    public static List<String> readFromLocalAfterUpload() throws IOException {
        String filePath = LOCAL_TEMP_PATH;
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 读取文件内容
     *
     * @param inputStream 输入流
     * @return 文件内容
     * @throws IOException 读取失败时抛出
     */
    private String readFileContent(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    public static void main(String[] args) {
        DhcpdConfigReaderBck reader = new DhcpdConfigReaderBck();
        try {
            // 示例: 开启 dev 模式读取
            reader.readDhcpdConfig("dev", "192.168.6.100", 22, "root", "Metoo89745000!");

            // 示例: 开启 prob 模式读取
            // 请确保文件已上传到 LOCAL_TEMP_PATH
            // reader.readDhcpdConfig("prob", null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
