package com.metoo.nrsm.core.config.ssh.demo;

import com.jcraft.jsch.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SimpleTelnetDemo {

    public static void main(String[] args) {
        try {
            connectToTelnet();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectToTelnet() throws JSchException, IOException {
        // Hardcoded connection details
        String host = "192.168.204.1";
        int port = 22;
        String username = "metoo";  // Change to your username
        String password = "metoo89745000";  // Change to your password

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);

        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setDaemonThread(true);

        System.out.println("Connecting to " + host + ":" + port);
        session.connect(30000);

        // Open shell channel (for Telnet)
        Channel channel = session.openChannel("shell");
        channel.connect(3000);

        System.out.println("Connected successfully");

        // Get input stream to read responses
        InputStream inputStream = channel.getInputStream();

        try {
            byte[] buffer = new byte[1024];
            int i;
            while ((i = inputStream.read(buffer)) != -1) {
                // Print the server response to console
                System.out.print(new String(buffer, 0, i));
            }
        } finally {
            // Clean up
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
            }
            System.out.println("\nDisconnected");
        }
    }
}
