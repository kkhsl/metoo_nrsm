package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.entity.Interface;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface InterfaceMapper {

    Interface selectObjById(Long id);

    List<Interface> selectObjConditionQuery(InterfaceDTO dto);

    List<Interface> selectObjByMap(Map params);

    int save(Interface instance);

    int update(Interface instance);

    int delete(Long id);

}
