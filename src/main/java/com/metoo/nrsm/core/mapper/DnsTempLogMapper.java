package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.DnsTempLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DnsTempLogMapper {
    /**
     * 保存临时数据
     * @param log
     */
    void saveInfo(DnsTempLog log);

    /**
     * 清空临时表数据
     */
    void truncateTable();

    /**
     * 获取解析日志汇总数据（只有ipv4记录）
     * @return
     */
    List<DnsTempLog> queryRecordInfo();
}
