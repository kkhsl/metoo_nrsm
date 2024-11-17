package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Unbound;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UnboundMapper {

    Unbound selectObjByOne(Map params);
    int save(Unbound instance);
    int update(Unbound instance);
    int delete(Long id);
}
