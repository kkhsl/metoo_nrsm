package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.LinkDTO;
import com.metoo.nrsm.entity.Link;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface LinkMapper {

    Link selectObjById(Long id);

    List<Link> selectObjConditionQuery(LinkDTO instance);

    List<Link> selectObjByMap(Map params);

    int save(Link instace);

    int update(Link instace);

    int delete(Long id);

    int batchesDel(Long[] ids);

    int batchesInsert(List<Link> instances);
}
