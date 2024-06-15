package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.dto.AddressPoolFixedDTO;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolFixed;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AddressPoolFixedMapper {

    AddressPoolFixed selectObjById(Long id);

    List<AddressPoolFixed> selectObjConditionQuery(AddressPoolFixedDTO dto);

    List<AddressPoolFixed> selectObjByMap(Map params);

    List<AddressPoolFixedVO> selectObjToVOByMap(Map params);

    int save(AddressPoolFixed instance);

    int update(AddressPoolFixed instance);

    int delete(Long id);

}
