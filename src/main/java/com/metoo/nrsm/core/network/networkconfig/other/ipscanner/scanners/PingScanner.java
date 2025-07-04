package com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingScanner implements Runnable {

    private final String ip;

    public PingScanner(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            String[] cmd = System.getProperty("os.name").toLowerCase().contains("win")
                    ? new String[]{"ping", "-n", "1", ip}
                    : new String[]{"ping", "-c", "1", ip};

            Process process = new ProcessBuilder(cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("TTL=") || line.contains("ttl=")) {
                    System.out.println("[PING] " + ip + " is reachable");
                    return;
                }
            }
            System.out.println("[PING] " + ip + " is unreachable");
        } catch (Exception e) {
            System.err.println("[PING] Error scanning " + ip + ": " + e.getMessage());
        }
    }
}
