package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.DiskMapper;
import com.metoo.nrsm.core.service.IDiskService;
import com.metoo.nrsm.core.utils.system.SystemUsageUtils;
import com.metoo.nrsm.core.vo.DiskVO;
import com.metoo.nrsm.entity.Disk;
import com.metoo.nrsm.entity.SystemUsage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Timestamp;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
public class DiskServiceImpl implements IDiskService {

    @Resource
    private DiskMapper diskMapper;

    @Override
    public void getRootDiskSpaceInformation() {

        // 获取当前 Java 项目运行的工作目录
        String currentDir = System.getProperty("user.dir");

        // 获取当前目录所在磁盘的根目录路径
        Path path = Paths.get(currentDir).toAbsolutePath().getRoot();

        // 如果根路径为 null，则抛出异常或返回一个默认值
        if (path == null) {
            // 可以抛出异常，或者返回一个默认值
            throw new IllegalStateException("无法获取根路径");
        }
        // 获取磁盘空间信息
        File file = path.toFile();
        long totalSpace = file.getTotalSpace(); // 总空间
        long usableSpace = file.getUsableSpace(); // 可用空间
        long freeSpace = file.getFreeSpace(); // 剩余空间
        long usedSpace = totalSpace - freeSpace; // 已使用空间

        // 查询数据库中是否已存在此根目录的记录
        Disk existingDisk = diskMapper.findByRootDirectory(path.toString());

        // 使用 Optional 包装 Disk 对象
        Optional<Disk> existingDiskOpt = Optional.ofNullable(existingDisk);

        Disk disk;
        if (existingDiskOpt != null && existingDiskOpt.isPresent()) {
            // 如果记录存在，更新磁盘信息
            disk = existingDiskOpt.get();
            disk.setTotalSpace(totalSpace);
            disk.setUsableSpace(usableSpace);
            disk.setFreeSpace(freeSpace);
            disk.setUsedSpace(usedSpace);
            disk.setUpdateTime(new Date()); // 更新时间
            System.out.println("更新根目录信息: " + path);
        } else {
            // 如果记录不存在，插入新的磁盘记录
            disk = new Disk();
            disk.setRootDirectory(path.toString());
            disk.setTotalSpace(totalSpace);
            disk.setUsableSpace(usableSpace);
            disk.setFreeSpace(freeSpace);
            disk.setUsedSpace(usedSpace);
            disk.setAddTime(new Date()); // 创建时间
            disk.setUpdateTime(new Date()); // 更新时间
            System.out.println("插入新根目录信息: " + path);
        }
        this.save(disk);
    }

    @Override
    public DiskVO getRootDisk() {
        // 获取当前 Java 项目运行的工作目录
        String currentDir = System.getProperty("user.dir");

        // 获取当前目录所在磁盘的根目录路径
        Path path = Paths.get(currentDir).toAbsolutePath().getRoot();

        // 如果根路径为 null，则抛出异常或返回一个默认值
        if (path == null) {
            // 你可以抛出异常，或者返回一个默认值
            throw new IllegalStateException("无法获取根路径");
        }
        Disk disk = diskMapper.findByRootDirectory(path.toString());
        // 使用 Optional 来避免 null 判断
        return Optional.ofNullable(disk)
                .map(d -> {
                    // 创建 DiskVO 对象
                    DiskVO diskVO = new DiskVO();
                    // 格式化并设置各个字段
                    diskVO.setTotalSpaceFormatted(formatSpace(d.getTotalSpace()));
                    diskVO.setUsableSpaceFormatted(formatSpace(d.getUsableSpace()));
                    diskVO.setFreeSpaceFormatted(formatSpace(d.getFreeSpace()));
                    diskVO.setUsedSpaceFormatted(formatSpace(d.getUsedSpace()));
                    return diskVO;
                })
                // 如果 disk 为 null，返回一个新的 DiskVO 对象（或者其他默认值）
                .orElse(new DiskVO());
    }

    // 这个方法用于将字节数转换为 KB, MB, GB 等
    private String formatSpace(long spaceInBytes) {
//        if (spaceInBytes < 1024) {
//            return spaceInBytes + " B";
//        } else if (spaceInBytes < 1024 * 1024) {
//            return String.format("%.2f KB", spaceInBytes / 1024.0);
//        } else if (spaceInBytes < 1024 * 1024 * 1024) {
//            return String.format("%.2f MB", spaceInBytes / (1024.0 * 1024));
//        } else {
//            return String.format("%.2f GB", spaceInBytes / (1024.0 * 1024 * 1024));
//        }
        if (spaceInBytes < 1024) {
            return spaceInBytes + " B";
        } else if (spaceInBytes < 1024 * 1024) {
            return String.format("%.2f KB", spaceInBytes / 1024.0);
        } else if (spaceInBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", spaceInBytes / (1024.0 * 1024));
        } else if (spaceInBytes < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2f GB", spaceInBytes / (1024.0 * 1024 * 1024));
        } else {
            return String.format("%.2f TB", spaceInBytes / (1024.0 * 1024 * 1024 * 1024));
        }

//        if (spaceInBytes < 1024) {
//            return spaceInBytes + " B";
//        } else if (spaceInBytes < 1024 * 1024) {
//            return String.format("%.2f KB", spaceInBytes / 1024.0);
//        } else if (spaceInBytes < 1024 * 1024 * 1024) {
//            return String.format("%.2f MB", spaceInBytes / (1024.0 * 1024));
//        }  else if (spaceInBytes < 1024 * 1024 * 1024 * 1024) {
//            return String.format("%.2f GB", spaceInBytes / (1024.0 * 1024 * 1024));
//        } else {
//            return String.format("%.2f TB", spaceInBytes / (1024.0 * 1024 * 10244 * 1024));
//        }
    }

    @Override
    public boolean save(Disk instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            try {

                this.diskMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                this.diskMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Disk instance) {
        try {
            this.diskMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
