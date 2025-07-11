package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;
import com.metoo.nrsm.entity.TerminalUnitSubnetV6;
import com.metoo.nrsm.entity.UnitSubnet;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface TerminalUnitMapper {

    TerminalUnit selectObjById(Long id);

    TerminalUnitSubnet selectV4ObjById(Long id);

    TerminalUnitSubnetV6 selectV6ObjById(Long id);

    List<TerminalUnitSubnet> selectV4ObjByTerminalUnitId(Long id);

    List<TerminalUnitSubnetV6> selectV6ObjByTerminalUnitId(Long id);

    List<TerminalUnit> selectObjAll();

    @Select("SELECT * FROM metoo_unit_subnet")
    List<UnitSubnet> selectAll();

    @Select("SELECT * FROM metoo_unit_subnet WHERE unitId = #{unitId}")
    List<UnitSubnet> selectByUnitId(Long id);

    @Select("SELECT * FROM metoo_unit_subnet WHERE id = #{id}")
    UnitSubnet findById(Long id);

    @Insert("INSERT INTO metoo_unit_subnet(addTime, name, ipv4, ipv6,vlan,unitId) VALUES(#{addTime}, #{name}, #{ipv4}, #{ipv6}, #{vlan}, #{unitId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UnitSubnet subnet);

    @Update("UPDATE metoo_unit_subnet SET addTime = #{addTime}, name = #{name}, ipv4 = #{ipv4}, ipv6 = #{ipv6},vlan=#{vlan},unitId=#{unitId} WHERE id = #{id}")
    int update(UnitSubnet subnet);

    @Delete("DELETE FROM metoo_unit_subnet WHERE id = #{id}")
    int deleteById(Long id);

    int insertTerminalUnit(TerminalUnit terminalUnit);

    int insertTerminalUnitV4(TerminalUnitSubnet terminalUnitSubnet);

    int insertTerminalUnitV6(TerminalUnitSubnetV6 terminalUnitSubnetV6);

    int updateTerminalUnit(TerminalUnit terminalUnit);

    int updateTerminalUnitV4(TerminalUnitSubnet terminalUnitSubnet);

    int updateTerminalUnitV6(TerminalUnitSubnetV6 terminalUnitSubnetV6);

    int deleteTerminalUnit(Long id);

    int deleteTerminalUnitV4(Long id);

    int deleteTerminalUnitV4ByTerminalUnitId(Long id);

    int deleteTerminalUnitV6(Long id);

    int deleteTerminalUnitV6ByTerminalUnitId(Long id);

    List<TerminalUnit> selectObjByMap(Map params);

    List<TerminalUnit> selectObjAndTerminalByMap(Map params);

    List<TerminalUnit> selectObjAndTerminalHistoryByMap(Map params);


}
