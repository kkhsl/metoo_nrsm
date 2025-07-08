package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.DnsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DnsRecordMapper {
    /**
     * 批量保存数据
     *
     * @param record
     */
    void saveInfo(List<DnsRecord> record);

    List<DnsRecord> queryRecordByTime(@Param("queryTime") String queryTime, @Param("topN") Integer topN);

}
