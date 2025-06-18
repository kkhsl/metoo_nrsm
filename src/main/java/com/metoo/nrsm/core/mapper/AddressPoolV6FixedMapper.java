package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.AddressPoolV6FixedDTO;
import com.metoo.nrsm.core.vo.AddressPoolV6FixedVO;
import com.metoo.nrsm.entity.AddressPoolV6Fixed;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-13 10:48
 */
@Mapper
public interface AddressPoolV6FixedMapper {

    AddressPoolV6Fixed selectObjById(Long id);

    List<AddressPoolV6Fixed> selectObjConditionQuery(AddressPoolV6FixedDTO dto);

    List<AddressPoolV6Fixed> selectObjByMap(Map params);

    List<AddressPoolV6FixedVO> selectObjToVOByMap(Map params);

    int save(AddressPoolV6Fixed instance);

    int update(AddressPoolV6Fixed instance);

    int delete(Long id);

}
