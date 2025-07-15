package com.metoo.nrsm.core.config.utils.gather.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.InputStream;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 22:34
 */
public class RemotePythonExecution {

    public static void main(String[] args) {
        String host = "192.168.5.101";
        String username = "root";
        String password = "metoo89745000";
        int port = 22; // SSH port

        String pythonScript = "/path/to/your/python_script.py";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // Avoid asking for key confirmation
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            // Executing command
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("python3 /opt/sqlite/controller/main.py h3c switch 192.168.100.1 ssh 22 metoo metoo89745000 aliveint");
            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            channel.connect();

            // Reading output
            byte[] tmp = new byte[1024];
            while (true) {
                while (stdout.available() > 0) {
                    int i = stdout.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                while (stderr.available() > 0) {
                    int i = stderr.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (stdout.available() > 0) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    System.out.println(ee);
                }
            }

            channel.disconnect();
            session.disconnect();

        } catch (JSchException | java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
