package com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NmapScanner implements Runnable {
    private final String ip;

    public NmapScanner(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Process process = new ProcessBuilder("nmap", "-sn", ip).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Host is up")) {
                    System.out.println("[NMAP] " + ip + " is reachable");
                    return;
                }
            }
            System.out.println("[NMAP] " + ip + " is unreachable");
        } catch (Exception e) {
            System.err.println("[NMAP] Error scanning " + ip + ": " + e.getMessage());
        }
    }
}