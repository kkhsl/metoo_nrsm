package com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class NmapScanner implements Runnable {
    private final String target;
    private final CountDownLatch latch;

    public NmapScanner(String target, CountDownLatch latch) {
        this.target = target;
        this.latch = latch;
    }


    public static void main(String[] args) throws IOException {
        Process process = new ProcessBuilder("C:\\Program Files (x86)\\Nmap\\nmap.exe", "-sn", "192.168.6.101").start();
//        Process process = new ProcessBuilder("nmap", "-sn", "192.168.6.101").start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.toLowerCase().contains("host is up")) {
                log.info("[NMAP] {} is reachable", line);
            }
        }
    }

    @Override
    public void run() {
        try {
//            Process process = new ProcessBuilder("C:\\Program Files (x86)\\Nmap\\nmap.exe", "-sn", target).start();
            log.info("[NMAP] {}", target);
            Process process = new ProcessBuilder("nmap", "-sn", target).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("host is up")) {
                    log.info("[NMAP] {} is reachable", line);
                    return;
                }
            }
            log.info("[NMAP] Scanning complete for target: {}", target);
        } catch (Exception e) {
            log.error("[NMAP] Error scanning {}: {}", target, e.getMessage());
        } finally {
            // 在扫描完成后，主线程通过 latch.countDown() 知道任务已完成
            if (latch != null) {
                latch.countDown();  // 任务完成后减少计数
            }
        }
    }
}