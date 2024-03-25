package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.Dhcp6Dto;
import com.metoo.nrsm.entity.Dhcp6;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-16 11:14
 */
@Mapper
public interface Dhcp6Mapper {

    Dhcp6 selectObjById(Long id);

    Dhcp6 selectObjByLease(String lease);

    List selectConditionQuery(Dhcp6Dto dto);

    List<Dhcp6> selectObjByMap(Map params);

    int save(Dhcp6 instance);

    int update(Dhcp6 instance);

    int delete(Long id);

    int truncateTable();

    int deleteTable();
}
