package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.DomainDTO;
import com.metoo.nrsm.entity.Domain;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

public interface IDomainService {

    Domain selectObjById(Long id);

    List<Domain> selectObjConditionQuery(DomainDTO dto);

    List<Domain> selectObjByMap(Map params);

    List<Domain> selectDomainAndVlanByMap(Map params);

    List<Domain> selectDomainAndVlanProceDureByMap(Map params);

    int save(Domain instance);

    int update(Domain instance);

    int delete(Long id);
}
