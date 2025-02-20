package com.metoo.nrsm.core.utils.system;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

public class SystemInfoExample {

    public static void main(String[] args) {
        // 创建 SystemInfo 实例来获取系统信息
        SystemInfo systemInfo = new SystemInfo();

        // 获取硬件信息
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        // 获取操作系统信息
        OperatingSystem os = systemInfo.getOperatingSystem();
        FileSystem fileSystem = os.getFileSystem();

        // 获取 CPU 使用情况
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);  // 等待 1 秒钟以便获取负载变化
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        // 获取内存信息
        long totalMemory = memory.getTotal();  // 总内存
        long availableMemory = memory.getAvailable();  // 可用内存
        double memoryUsage = 100.0 * (1 - ((double) availableMemory / (double) totalMemory));


        // 打印信息
        System.out.println("CPU 使用率: " + String.format("%.2f", cpuLoad) + "%");
        System.out.println("总内存: " + totalMemory / (1024 * 1024 * 1024) + " GB");
        System.out.println("可用内存: " + availableMemory / (1024 * 1024 * 1024) + " GB");
        System.out.println("内存使用率: " + String.format("%.2f", memoryUsage) + "%");
    }
}
