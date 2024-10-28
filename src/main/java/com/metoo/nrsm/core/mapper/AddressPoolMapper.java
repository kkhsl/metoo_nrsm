package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.AddressPool;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AddressPoolMapper {

    AddressPool selectObjById(Long id);

    List<AddressPool> selectObjConditionQuery(AddressPoolDTO dto);

    List<AddressPool> selectObjByMap(Map params);

    List<AddressPoolVO> selectObjToVOByMap(Map params);

    int save(AddressPool instance);

    int update(AddressPool instance);

    int delete(Long id);

}
