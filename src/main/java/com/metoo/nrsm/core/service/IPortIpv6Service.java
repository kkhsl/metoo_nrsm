package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.PortIpv6;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:28
 */
public interface IPortIpv6Service {

    List<PortIpv6> selectObjByMap(Map params);

    List<PortIpv6> selectObjByDeviceUuid(String deviceUuid);

    boolean save(PortIpv6 instance);

    boolean update(PortIpv6 instance);

    boolean batchSave(PortIpv6 instance);

    boolean batchSaveGather(List<PortIpv6> instance);

    boolean truncateTableGather();

    boolean deleteTable();

    boolean copyGatherDataToPortIpv6();

    boolean copyGatherData();

    List<PortIpv6> selctVlanNumberByREGEXPREPLACE();

    List<PortIpv6> selctVlanNumberBySplitFieldFunction();

}
