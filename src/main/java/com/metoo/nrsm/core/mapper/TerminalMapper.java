package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.TerminalDTO;
import com.metoo.nrsm.entity.Terminal;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TerminalMapper {

    Terminal selectObjById(Long id);

    List<Terminal> selectObjByConditionQuery(TerminalDTO instance);

    List<Terminal> selectObjByMap(Map params);

    List<Terminal> selectObjToProbe(Map params);

    List<Terminal> selectObjHistoryByMap(Map params);

    List<Terminal> selectObjIntersection();

    List<Terminal> selectObjLeftdifference();

    List<Terminal> selectObjRightdifference();

    List<Terminal> selectV4ipIsNullAndV6ipIsNull();

    List<Terminal> selectDeviceIpByNSwitch();

    List<Terminal> selectObjByNeIp();

    List<Terminal> selectObjByVM();

    List<Terminal> selectNSwitchToTopology(Map params);

    List<Terminal> selectHistoryNSwitchToTopology(Map params);

  int save(Terminal instance);

    int update(Terminal instance);

    int batchSave(List<Terminal> instance);

    int batchUpdate(List<Terminal> instance);

    List<Terminal> selectVMHost();

    boolean updateVMHostDeviceType();

    boolean updateVMDeviceType();

    boolean updateVMDeviceIp();

    int delete(Long id);

    int deleteObjByType(Integer id);

    int copyTerminalToTerminalHistory();

    Map<String, Integer> terminalCount();
}
