package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Port;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:37
 */
@Mapper
public interface PortMapper {

    List selectObjByMap(Map params);

    List selectObjByDeviceUuid(String deviceUuid);

    List selectSameSubnetWithTwoPortsNotBothVlan();

    int save(Port instance);

    int update(Port instance);

    int batchSave(Port instance);

    int batchSaveGather(List<Port> instance);

    int truncateTableGather();

    int deleteTable();

    int copyGatherDataToPort();

    List<Port> selctVlanNumberByREGEXPREPLACE();

    List<Port> selctVlanNumberBySplitFieldFunction();


}
