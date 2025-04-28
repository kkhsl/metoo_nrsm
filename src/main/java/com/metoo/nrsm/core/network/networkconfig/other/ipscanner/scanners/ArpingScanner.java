package com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ArpingScanner implements Runnable {

    private final String ip;

    public ArpingScanner(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Process process = new ProcessBuilder("arping", "-c", "1", ip).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Unicast reply")) {
                    System.out.println("[ARPING] " + ip + " is reachable");
                    return;
                }
            }
            System.out.println("[ARPING] " + ip + " is unreachable");
        } catch (Exception e) {
            System.err.println("[ARPING] Error scanning " + ip + ": " + e.getMessage());
        }
    }
}
