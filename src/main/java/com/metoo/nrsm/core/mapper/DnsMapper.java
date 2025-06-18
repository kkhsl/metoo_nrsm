package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Dns;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-27 14:23
 */
@Mapper
public interface DnsMapper {

    List<Dns> selectObjByMap(Map params);

    List<Dns> selectObjByPrimaryDomain(String primaryDomain);

    boolean save(Dns instance);

    boolean update(Dns instance);

    boolean delete(Long id);
}
