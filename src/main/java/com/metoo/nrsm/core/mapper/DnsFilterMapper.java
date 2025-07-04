package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.DnsFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DnsFilterMapper {

    List<DnsFilter> selectAll(Map params);

    DnsFilter selectById(Long id);

    DnsFilter selectByDomainName(String domainName);

    int save(DnsFilter instance);

    int update(DnsFilter instance);

    int delete(Long id);
}
