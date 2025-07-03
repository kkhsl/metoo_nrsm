package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Port;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:28
 */
public interface IPortService {

    List<Port> selectObjByMap(Map params);

    List<Port> selectObjByDeviceUuid(String deviceUuid);

    List<Port> selectSameSubnetWithTwoPortsNotBothVlan();

    boolean save(Port instance);

    boolean update(Port instance);

    boolean batchSave(Port instance);

    boolean batchSaveGather(List<Port> instance);

    boolean truncateTableGather();

    boolean deleteTable();

    boolean copyGatherDataToPort();

    boolean copyGatherData();

    List<Port> selctVlanNumberByREGEXPREPLACE();

    List<Port> selctVlanNumberBySplitFieldFunction();

}
