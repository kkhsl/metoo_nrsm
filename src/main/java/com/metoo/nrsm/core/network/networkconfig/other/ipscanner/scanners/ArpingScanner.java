package com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
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
                    log.info("[ARPING] {} {}", ip, " is reachable");
                    return;
                }
            }
            log.info("[ARPING] {} {}", ip, " is unreachable");
        } catch (Exception e) {
            log.info("[ARPING] {} {}", ip, e.getMessage());
        }
    }
}
