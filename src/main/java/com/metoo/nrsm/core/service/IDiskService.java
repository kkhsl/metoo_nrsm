package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.vo.DiskVO;
import com.metoo.nrsm.entity.Disk;

public interface IDiskService {

    void getRootDiskSpaceInformation();

    // 查询根目录磁盘信息
    DiskVO getRootDisk();

    boolean save(Disk instance);

    boolean update(Disk instance);
}
