package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Disk;

import java.util.Optional;

public interface DiskMapper {

    Disk findByRootDirectory(String path);

    int save(Disk instance);

    int update(Disk instance);
}
