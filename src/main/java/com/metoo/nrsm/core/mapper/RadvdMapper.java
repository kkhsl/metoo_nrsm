package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.RadvdDTO;
import com.metoo.nrsm.entity.Radvd;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RadvdMapper {

    Radvd selectObjById(Long id);

    Radvd selectObjByName(String name);

    List<Radvd> selectObjByMap(Map params);

    List<Radvd> selectObjConditionQuery(RadvdDTO dto);

    int save(Radvd instance);

    int update(Radvd instance);

    int delete(Long id);
}
