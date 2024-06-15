package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.dto.AddressPoolFixedDTO;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.AddressPoolFixed;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 9:46
 */
public interface IAddressPoolFixedService {

    AddressPoolFixed selectObjById(Long id);

    Page<AddressPoolFixed> selectObjConditionQuery(AddressPoolFixedDTO dto);

    List<AddressPoolFixed> selectObjByMap(Map params);

    List<AddressPoolFixedVO> selectObjToVOByMap(Map params);

    int save(AddressPoolFixed instance);

    int update(AddressPoolFixed instance);

    int delete(Long id);

    void write();
}
