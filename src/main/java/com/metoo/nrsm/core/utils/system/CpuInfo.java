package com.metoo.nrsm.core.utils.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class CpuInfo {

    public static void main(String[] args) {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // 获取系统的 CPU 核心数
        int availableProcessors = osBean.getAvailableProcessors();
        System.out.println("Available processors (cores): " + availableProcessors);

        // 获取系统的负载
        double systemLoad = osBean.getSystemLoadAverage();
        System.out.println("System load average: " + systemLoad);
    }
}
