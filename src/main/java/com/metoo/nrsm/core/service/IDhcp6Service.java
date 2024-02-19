package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.Dhcp6Dto;
import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.entity.nspm.Dhcp;
import com.metoo.nrsm.entity.nspm.Dhcp6;
import com.metoo.nrsm.entity.nspm.Internet;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:07
 */

public interface IDhcp6Service {

    Dhcp6 selectObjById(Long id);

    Dhcp6 selectObjByLease(String lease);

    Page<Dhcp6> selectConditionQuery(Dhcp6Dto dto);

    List<Dhcp6> selectObjByMap(Map params);

    boolean save(Dhcp6 instance);

    boolean update(Dhcp6 instance);

    boolean delete(Long id);

    boolean truncateTable();

    void gather(Date time);
}
