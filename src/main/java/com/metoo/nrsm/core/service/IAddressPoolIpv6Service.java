package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.dto.AddressPoolIpv6DTO;
import com.metoo.nrsm.core.vo.AddressPoolIpv6VO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolIpv6;

import java.util.List;
import java.util.Map;

public interface IAddressPoolIpv6Service {

    AddressPoolIpv6 selectObjById(Long id);

    Page<AddressPoolIpv6> selectObjConditionQuery(AddressPoolIpv6DTO dto);

    List<AddressPoolIpv6> selectObjByMap(Map params);

    List<AddressPoolIpv6VO> selectObjToVOByMap(Map params);

    int save(AddressPoolIpv6 instance);

    int update(AddressPoolIpv6 instance);

    int delete(Long id);

    void write();


}
