package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.FtpConfig;

import java.util.List;

public interface FtpConfigMapper {
    // 新增FTP配置
    int insert(FtpConfig config);

    // 按ID更新FTP配置
    int update(FtpConfig config);

    // 查询所有有效配置（未逻辑删除）
    List<FtpConfig> selectAllActive();

    // 按ID查询配置（忽略删除状态）
    FtpConfig selectById(Integer id);


}