package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.AddressPoolFixedDTO;
import com.metoo.nrsm.core.dto.AddressPoolV6FixedDTO;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.AddressPoolV6FixedVO;
import com.metoo.nrsm.entity.AddressPoolFixed;
import com.metoo.nrsm.entity.AddressPoolV6Fixed;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 9:46
 */
public interface IAddressPoolV6FixedService {

    AddressPoolV6Fixed selectObjById(Long id);

    Page<AddressPoolV6Fixed> selectObjConditionQuery(AddressPoolV6FixedDTO dto);

    List<AddressPoolV6Fixed> selectObjByMap(Map params);

    List<AddressPoolV6FixedVO> selectObjToVOByMap(Map params);

    int save(AddressPoolV6Fixed instance);

    int update(AddressPoolV6Fixed instance);

    int delete(Long id);

    void write();
}
