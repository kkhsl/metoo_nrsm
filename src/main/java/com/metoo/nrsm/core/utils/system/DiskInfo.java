package com.metoo.nrsm.core.utils.system;

import com.metoo.nrsm.entity.Disk;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.software.os.FileSystem;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class DiskInfo {

    public static void main(String[] args) {
        getDiskSpaceInformation();
    }


    // 获取跟文件系统 '/'
    public static void getDiskSpaceInformation() {
        SystemInfo si = new SystemInfo();
        FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
        List<HWDiskStore> diskStores = si.getHardware().getDiskStores();

        // 获取磁盘数量和详细信息
        System.out.println("磁盘数量: " + diskStores.size());
        for (HWDiskStore disk : diskStores) {
            log.info("磁盘名称: " + disk.getModel());
            log.info("磁盘大小: " + disk.getSize() / (1024 * 1024 * 1024) + " GB");
        }

        // 获取根目录磁盘信息
        fileSystem.getFileStores().forEach(fileStore -> {
            if (fileStore.getMount().equals("/")) {
                log.info("根目录总空间: " + fileStore.getTotalSpace() / (1024 * 1024 * 1024) + " GB");
                log.info("根目录可使用空间: " + fileStore.getUsableSpace() / (1024 * 1024 * 1024) + " GB");
                log.info("根目录剩余空间: " + fileStore.getTotalSpace() / (1024 * 1024 * 1024) + " GB");
            }
        });
    }

    @Test
    public void getRootDiskSpaceInformationTest() {
        getRootDiskSpaceInformation();
    }

    public static void getRootDiskSpaceInformation() {
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


    }

    /**
     * df -h：查看磁盘的总容量和已使用容量
     * lsblk：命令查看所有磁盘的挂载情况
     */
    public static void getAllRootDiskSpaceInformation() {
        // 获取所有挂载的根目录
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();

        for (Path path : rootDirectories) {
            // 输出根目录
            System.out.println("根目录: " + path);

            // 获取磁盘空间信息
            File file = path.toFile();
            long totalSpace = file.getTotalSpace(); // 总空间
            long usableSpace = file.getUsableSpace(); // 可用空间
            long freeSpace = file.getFreeSpace(); // 剩余空间
            long used = totalSpace - freeSpace;// 已使用空间


            // 打印磁盘空间信息
            log.info("根目录总空间: " + totalSpace / (1024 * 1024 * 1024) + " GB");
            log.info("根目录可使用空间: " + usableSpace / (1024 * 1024 * 1024) + " GB");
            log.info("根目录剩余空间: " + freeSpace / (1024 * 1024 * 1024) + " GB");
            log.info("根目录已使用空间: " + used / (1024 * 1024 * 1024) + " GB");
        }
    }

}
