package com.metoo.nrsm.core.system.service.remote;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.system.service.exception.RemoteOperationException;
import com.metoo.nrsm.core.system.service.model.CommandResult;
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
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final int timeout;

    public SshCommandExecutor(String host, int port, String username,
                              String password, int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * 执行远程命令
     *
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

            // 3. 配置SSH会话参数 - 不检查主机密钥(生产环境应配置为检查)
            session.setConfig("StrictHostKeyChecking", "no");

            // 4. 连接会话
            session.connect(timeout);

            // 5. 创建执行通道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            // 6. 获取命令输出流和错误输出流
            InputStream in = channel.getInputStream();
            InputStream errStream = channel.getErrStream();

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
                    Thread.currentThread().interrupt();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errStream))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorOutput.append(errorLine).append("\n");
                    }
                } catch (IOException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // 启动两个线程
            outputThread.start();
            errorThread.start();

            // 等待两个线程完成
            outputThread.join();
            errorThread.join();

            // 8. 获取命令退出状态
            int exitStatus = channel.getExitStatus();

            // 9. 处理不同的命令退出状态
            if (command.contains("systemctl status ")) {
                /*
                 * systemctl status 命令的特殊退出码处理:
                 * 0: 服务正在运行
                 * 1: 服务未运行
                 * 2: 服务状态未知
                 * 3: 服务已停止
                 * 4: 服务不存在
                 */
                if (exitStatus == 4) {
                    throw new RemoteOperationException("服务不存在");
                }
                // 其他状态码(0-3)都是正常情况，不抛出异常
            } else {
                // 对于非status命令，只有退出码0表示成功
                if (exitStatus != 0) {
                    throw new RemoteOperationException(
                            String.format("命令执行失败，退出码: %d\n错误输出: %s",
                                    exitStatus, errorOutput.toString()));
                }
            }

            // 10. 返回命令输出
            return output.toString();

        } catch (JSchException e) {
            throw new RemoteOperationException("SSH连接失败: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RemoteOperationException("IO操作失败: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteOperationException("命令执行被中断", e);
        } finally {
            // 11. 清理资源
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * 执行命令并返回包含状态码的结果对象
     */
    public CommandResult executeCommandWithStatus(String command) throws RemoteOperationException {
        String output = executeCommand(command);
        // 注意：这里需要修改实现以获取退出状态码
        // 实际实现可能需要额外逻辑来保存退出状态码
        return new CommandResult(output, 0); // 简化示例
    }
}
