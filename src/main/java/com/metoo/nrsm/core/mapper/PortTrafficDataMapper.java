package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.PortTrafficData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortTrafficDataMapper {

    /**
     * 批量插入端口流量数据
     */
    void batchInsert(@Param("list") List<PortTrafficData> dataList);

    /**
     * 查询特定端口的最近流量记录（带时间限制）
     */
    List<PortTrafficData> findByDeviceAndPortAndTime(
            @Param("deviceId") Long deviceId,
            @Param("ipAddress") String ipAddress,
            @Param("portIndex") String portIndex,
            @Param("isIpv6") boolean isIpv6,
            @Param("minTimestamp") long minTimestamp);

    /**
     * 查询特定端口的最后一条记录（不考虑时间）
     */
    List<PortTrafficData> findLastByDeviceAndPort(
            @Param("deviceId") Long deviceId,
            @Param("ipAddress") String ipAddress,
            @Param("portIndex") String portIndex,
            @Param("isIpv6") boolean isIpv6);

    /**
     * 删除早于指定时间戳的记录
     */
    void deleteByTimestampBefore(@Param("timestamp") long timestamp);
}