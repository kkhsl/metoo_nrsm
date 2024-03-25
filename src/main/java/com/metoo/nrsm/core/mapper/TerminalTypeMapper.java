package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TerminalTypeMapper {

    TerminalType selectObjById(Long id);

    TerminalType selectObjByType(Integer type);

    List<TerminalType> selectObjByMap(Map params);

    List<TerminalType> selectObjAll();
}
