package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.entity.Dhcp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-16 11:14
 */
@Mapper
public interface DhcpMapper {

    Dhcp selectObjById(Long id);

    Dhcp selectObjByLease(String lease);

    List selectConditionQuery(DhcpDto dto);

    List<Dhcp> selectObjByMap(Map params);

    int save(Dhcp instance);

    int update(Dhcp instance);

    int delete(Long id);

    int truncateTable();

    int deleteTable();

}
