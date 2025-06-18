package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.SubnetIpv6;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-24 14:41
 */
public interface ISubnetIpv6Service {

    SubnetIpv6 selectObjById(Long id);

    List<SubnetIpv6> selectSubnetByParentId(Long id);

    boolean save(SubnetIpv6 instance);

    Result update(SubnetIpv6 instance);

    Result getSubnet();

    int truncateTable();
}
