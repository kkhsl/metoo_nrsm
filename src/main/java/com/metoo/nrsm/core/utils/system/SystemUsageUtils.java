package com.metoo.nrsm.core.utils.system;

import com.metoo.nrsm.entity.Disk;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

@Slf4j
public class SystemUsageUtils {

    // CPU使用率
    // TODO: 暂时仅限linux
    public static double getCpuUsagePercentage() {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        // 获取系统的加载信息
        double systemLoad = bean.getSystemLoadAverage();
        double availableProcessors = bean.getAvailableProcessors();

        // 计算CPU使用率
        double cpuLoadAll = (systemLoad / availableProcessors) * 100;

        // 格式化为保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");

        return Double.parseDouble(df.format(cpuLoadAll));
    }

    // 内存使用率
    public static double getMemUsagePercentage() {
        // 创建 SystemInfo 实例来获取系统信息
        SystemInfo systemInfo = new SystemInfo();

        GlobalMemory memory = systemInfo.getHardware().getMemory();

        // 获取内存信息
        long totalMemory = memory.getTotal();  // 总内存
        long availableMemory = memory.getAvailable();  // 可用内存
        double memoryUsage = 100.0 * (1 - ((double) availableMemory / (double) totalMemory));
        // 格式化为保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");

        return Double.parseDouble(df.format(memoryUsage));
    }


    /**
     * df -h：查看磁盘的总容量和已使用容量
     * lsblk：命令查看所有磁盘的挂载情况
     */
    public static Disk getRootDiskSpaceInformation() {
        // 获取当前 Java 项目运行的工作目录
        String currentDir = System.getProperty("user.dir");

        // 获取当前目录所在磁盘的根目录路径
        Path path = Paths.get(currentDir).toAbsolutePath().getRoot();

        // 输出根目录
        log.info("当前项目所在磁盘根目录: " + path);

        // 获取磁盘空间信息
        File file = path.toFile();
        long totalSpace = file.getTotalSpace(); // 总空间
        long usableSpace = file.getUsableSpace(); // 可用空间
        long freeSpace = file.getFreeSpace(); // 剩余空间
        long used = totalSpace - freeSpace; // 已使用空间

        // 打印磁盘空间信息
        log.info("根目录总空间: " + totalSpace / (1024 * 1024 * 1024) + " GB");
        log.info("根目录可使用空间: " + usableSpace / (1024 * 1024 * 1024) + " GB");
        log.info("根目录剩余空间: " + freeSpace / (1024 * 1024 * 1024) + " GB");
        log.info("根目录已使用空间: " + used / (1024 * 1024 * 1024) + " GB");

        // TODO: 只记录一条数据
        Disk disk = new Disk();
        disk.setTotalSpace(totalSpace);
        disk.setUsableSpace(usableSpace);
        disk.setFreeSpace(freeSpace);
        disk.setUsedSpace(usableSpace);
        return disk;
    }

    public static void main(String[] args) {
        // CPU使用率
        System.out.println("CPU使用率: " + getCpuUsagePercentage() + "%");

        // 内存使用率
        System.out.println("内存使用率: " + getMemUsagePercentage() + "%");
    }


}