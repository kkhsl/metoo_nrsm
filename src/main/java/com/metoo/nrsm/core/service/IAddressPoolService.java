package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.AddressPool;

import java.util.List;
import java.util.Map;

public interface IAddressPoolService {

    AddressPool selectObjById(Long id);

    Page<AddressPool> selectObjConditionQuery(AddressPoolDTO dto);

    List<AddressPool> selectObjByMap(Map params);

    List<AddressPoolVO> selectObjToVOByMap(Map params);

    int save(AddressPool instance);

    int update(AddressPool instance);

    int delete(Long id);

    void write();


}
