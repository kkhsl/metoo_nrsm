package com.metoo.nrsm.core.utils.command;

import com.jcraft.jsch.*;
import com.metoo.nrsm.core.utils.Global;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DhcpdConfigReader {

    /**
     * 读取 dhcpd.conf 文件
     * @param mode 模式 ("dev" 或 "prob")
     * @param host 远程主机地址 (仅 dev 模式需要)
     * @param username 远程主机用户名 (仅 dev 模式需要)
     * @param password 远程主机密码 (仅 dev 模式需要)
     * @throws Exception 读取或文件传输失败时抛出
     */
    public List<String> readDhcpdConfig(String mode, String host, Integer port, String username, String password, String path) throws Exception {
        if ("dev".equalsIgnoreCase(mode)) {
            return readFromRemote(host, port, username, password, path);
        } else if (!"dev".equalsIgnoreCase(mode)) {
            return readFromLocalAfterUpload(path);
        } else {
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }

    /**
     * 从远程服务器读取 dhcpd.conf 文件
     */
    private List<String>  readFromRemote(String host, Integer port, String username, String password, String path) throws Exception {
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

            InputStream inputStream = channelSftp.get(path);

//            System.out.println("Content as String:");
//            System.out.println(readFileContentAsString(inputStream));
//
//            inputStream = channelSftp.get(REMOTE_FILE_PATH); // Reset input stream
//            System.out.println("Content as List:");
//
            List<String> lines = readFileContentAsList(inputStream);
//            lines.forEach(System.out::println);

            return lines;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 上传后从本地读取 dhcpd.conf 文件
     */
    private List<String> readFromLocalAfterUpload(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            List<String> lines = readFileContentAsList(Files.newInputStream(path));
            return lines;
        } else {
            throw new FileNotFoundException("Local temp file not found: " + path);
        }
    }

    /**
     * 读取文件内容为 String
     */
    private String readFileContentAsString(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 读取文件内容为 List<String>
     */
    private List<String> readFileContentAsList(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static void main(String[] args) {
        DhcpdConfigReader reader = new DhcpdConfigReader();
        try {
            // 示例: 开启 dev 模式读取
            List<String> lines = reader.readDhcpdConfig("dev", "192.168.6.101", 22, "root", "Metoo89745000!", "/etc/dhcp/dhcpd.conf");
            for (String line : lines) {
                System.out.println(line);
            }
            // 示例: 开启 prob 模式读取
            // reader.readDhcpdConfig("prob", null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
