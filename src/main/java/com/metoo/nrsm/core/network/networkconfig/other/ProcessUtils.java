package com.metoo.nrsm.core.network.networkconfig.other;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.concurrent.TimeUnit;

public class ProcessUtils {
    private static final SystemInfo si = new SystemInfo();
    private static final OperatingSystem os = si.getOperatingSystem();

    public static void monitorProcess(String processName) {
        int targetPid = -1;

        // 首次扫描查找目标进程
        for (OSProcess p : os.getProcesses()) {
            if (p.getName().contains(processName)) {
                targetPid = p.getProcessID();
                System.out.println("发现目标进程 PID: " + targetPid);
                break;
            }
        }

        if (targetPid == -1) {
            System.out.println("未找到目标进程");
            return;
        }

        // 监控循环
        while (true) {
            try {
                OSProcess process = os.getProcess(targetPid);
                String status = process.getState().name();

                System.out.println("进程状态: " + status);
                if (isProcessActive(status)) {
                    System.out.println("进程正在运行");
                } else {
                    System.out.println("进程已停止");
                    break;
                }

                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                System.out.println("进程监控异常: " + e.getMessage());
                break;
            }
        }
    }

    // 判断进程是否活跃 (跨平台状态映射)
    private static boolean isProcessActive(String state) {
        return state.equals(OSProcess.State.RUNNING.name()) ||
                state.equals(OSProcess.State.SLEEPING.name());
    }

    public static void main(String[] args) {
        monitorProcess("nrsm.jar"); // 监控包含该名称的进程
    }
}