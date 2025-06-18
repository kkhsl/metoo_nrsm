package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.AddressPoolIpv6DTO;
import com.metoo.nrsm.core.vo.AddressPoolIpv6VO;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-09 15:17
 */
@Mapper
public interface AddressPoolIpv6Mapper {

    AddressPoolIpv6 selectObjById(Long id);

    List<AddressPoolIpv6> selectObjConditionQuery(AddressPoolIpv6DTO dto);

    List<AddressPoolIpv6> selectObjByMap(Map params);

    List<AddressPoolIpv6VO> selectObjToVOByMap(Map params);

    int save(AddressPoolIpv6 instance);

    int update(AddressPoolIpv6 instance);

    int delete(Long id);
}
