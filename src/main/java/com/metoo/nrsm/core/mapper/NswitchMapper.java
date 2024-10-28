package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Nswitch;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-17 17:05
 */
public interface NswitchMapper {

    Nswitch selectObjByName(String name);

    List<Nswitch> selectObjAll();

    int save(Nswitch instance);
}
