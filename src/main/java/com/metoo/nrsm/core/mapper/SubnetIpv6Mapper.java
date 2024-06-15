package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.SubnetIpv6;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-24 15:20
 */
@Mapper
public interface SubnetIpv6Mapper {

    SubnetIpv6 selectObjById(Long id);

    List<SubnetIpv6> selectSubnetByParentId(@Param("parentId") Long parentId);

    int save(SubnetIpv6 instance);

    int update(SubnetIpv6 instance);

    int truncateTable();
}
