package com.metoo.nrsm.core.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MacTestMapper {
    void callRemoteIPAndPort();

    // 统计存在分组记录数>2的设备数量
    int countMultiRecordDevices();

    void updateToAEForMultiples(); // 步骤2（针对记录数>2的组）

    void updateToDEForMultiples(); // 步骤3（针对记录数>2的组）

    void swapAEtoDEForMultiples(); // 步骤4（针对记录数>2的组）

    void updateToXEForMultiples(); // 步骤5（针对记录数>2的组）

    void updatePairedDEForTwo();   // 步骤6（处理记录数=2的组）
}
