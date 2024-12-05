package com.metoo.nrsm.core.config.utils.gather.ssh;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 10:11
 */

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class SSHUtils2 {

    private String hostname;
    private Integer port;
    private String username;
    private String password;

    @Autowired
    public SSHUtils2(@Value("${ssh.hostname}")String hostname, @Value("${ssh.port}")Integer port,
                     @Value("${ssh.username}")String username, @Value("${ssh.password}")String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

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
                if (reader != null){
                    reader.close();
                }
                if (stdout != null){
                    stdout.close();
                };
                if (session != null){
                    session.close();
                };
                if (connection != null){
                    connection.close();
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputBuffer.toString();
    }

}