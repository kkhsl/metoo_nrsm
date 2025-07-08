package com.metoo.nrsm.core.config.utils.gather.ssh;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 10:11
 */

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommand;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class SSHUtils {

    //    @Value("${ssh.hostname}")
    private String hostname;
    //    @Value("${ssh.port}")
    private Integer port;
    //    @Value("${ssh.username}")
    private String username;
    //    @Value("${ssh.password}")
    private String password;

    @Autowired
    public SSHUtils(@Value("${ssh.hostname}") String hostname, @Value("${ssh.port}") Integer port,
                    @Value("${ssh.username}") String username, @Value("${ssh.password}") String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Test
    public void testExecuteCommand() {
        this.executeCommand("cd /opt/netmap/os-scanner/os-scanner4 && ./OS-scanner -i 192.168.100.1 -o 443 -c 1");
    }

    ;

    public String executeCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();
        Connection connection = null;
        Session session = null;
        InputStream stdout = null;
        BufferedReader reader = null;

        try {
            connection = new Connection(hostname, port);
            connection.connect();
            boolean isAuthenticated = connection.authenticateWithPassword(username, password);

            if (!isAuthenticated) {
                throw new IOException("Authentication failed.");
            }

            session = connection.openSession();

            session.execCommand(command);

            stdout = session.getStdout();
            reader = new BufferedReader(new InputStreamReader(stdout));
//            String output = readInputStream(stdout);

            String line;
            while ((line = reader.readLine()) != null) {
                // 不追加换行符，只接受py脚本返回的结果
//                outputBuffer.append(line).append("\n");
                outputBuffer.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (stdout != null) {
                    stdout.close();
                }
                ;
                if (session != null) {
                    session.close();
                }
                ;
                if (connection != null) {
                    connection.close();
                }
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputBuffer.toString();
    }

    private static void waitForCommandCompletion(Session session) throws IOException {
        // 等待命令完成（可选）
        while (session.getExitStatus() == 0) {
            try {
                Thread.sleep(100); // 根据需要调整睡眠时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("等待命令完成时线程被中断.", e);
            }
        }
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String hostname = "192.168.5.101";
        String username = "root";
        String password = "metoo89745000";

        PyCommand pyCommand = new PyCommand();
        pyCommand.setPrefix("cd /opt/sqlite/script &&");
        pyCommand.setVersion("python3");
        pyCommand.setPath("/opt/sqlite/script/");
        pyCommand.setName("main.py");
        pyCommand.setParams(new String[]{"h3c", "switch", "192.168.100.1", "ssh", "22", "metoo", "metoo89745000", "aliveint"});
        String command = pyCommand.toParamsString();

        SSHUtils sshUtils = new SSHUtils(hostname, 22, username, password);
        String output = sshUtils.executeCommand(command);
        System.out.println("Command output:");
        System.out.println(output);
    }
}