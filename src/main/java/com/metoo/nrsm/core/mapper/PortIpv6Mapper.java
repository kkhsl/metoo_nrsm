package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.PortIpv6;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:37
 */
@Mapper
public interface PortIpv6Mapper {

    List selectObjByMap(Map params);

    /**
     * 获取标准格式的IPv6地址列表
     *
     * @return CIDR格式列表（例：240e:380:11d::/64）
     */
    List<String> selectIpv6Cidrs();

    List selectObjByDeviceUuid(String deviceUuid);

    int save(PortIpv6 instance);

    int update(PortIpv6 instance);

    int batchSave(PortIpv6 instance);

    int batchSaveGather(List<PortIpv6> instance);

    int truncateTableGather();

    int deleteTable();

    int copyGatherDataToPortIpv6();

    List<PortIpv6> selctVlanNumberByREGEXPREPLACE();

    List<PortIpv6> selctVlanNumberBySplitFieldFunction();


}
