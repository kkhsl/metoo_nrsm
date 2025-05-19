package com.metoo.nrsm.core.system.service.remote;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.system.service.exception.RemoteOperationException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
/**
 * SSH远程命令执行工具
 * 封装了通过SSH协议执行远程命令的功能
 */
@Slf4j
public class SshCommandExecutor {
    private final String host;      // 远程主机地址
    private final int port;         // SSH端口，默认22
    private final String username;  // SSH用户名
    private final String password;  // SSH密码
    private final int timeout;      // 连接超时时间(毫秒)

    /**
     * 构造方法
     * @param host 远程主机地址
     * @param port SSH端口
     * @param username SSH用户名
     * @param password SSH密码
     * @param timeout 连接超时时间(毫秒)
     */
    public SshCommandExecutor(String host, int port, String username, String password, int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * 执行远程命令
     * @param command 要执行的命令
     * @return 命令输出结果
     * @throws RemoteOperationException 当执行失败时抛出
     */
    public String executeCommand(String command) throws RemoteOperationException {
        Session session = null;
        ChannelExec channel = null;
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try {
            // 1. 创建JSch实例
            JSch jsch = new JSch();

            // 2. 创建SSH会话
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // 3. 配置SSH会话参数
            session.setConfig("StrictHostKeyChecking", "no");

            // 4. 连接会话
            session.connect(timeout);

            // 5. 创建执行通道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);  // 设置要执行的命令
            channel.setInputStream(null); // 不发送输入

            // 6. 获取命令输出流和错误输出流
            InputStream in = channel.getInputStream();
            InputStream errStream = channel.getErrStream();  // 错误输出流

            // 7. 连接通道并执行命令
            channel.connect();

            // 使用两个线程分别读取标准输出和错误输出
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errStream))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorOutput.append(errorLine).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 启动两个线程
            outputThread.start();
            errorThread.start();

            // 等待两个线程完成
            outputThread.join();
            errorThread.join();

            // 9. 检查命令退出状态
            if (channel.getExitStatus() != 0) {
                throw new RemoteOperationException(
                        String.format("命令执行失败，退出码: %d\n错误输出: %s", channel.getExitStatus(), errorOutput.toString()));
            }

            // 10. 返回命令输出
            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "执行失败: " + e.getMessage();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

    }
}
