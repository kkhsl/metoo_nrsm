package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.DnsFilter;

import java.util.List;
import java.util.Map;

public interface IDnsFilterService {

    boolean saveDnsFilter(DnsFilter instance);

    List<DnsFilter> selectAll(Map params);

    DnsFilter updateDNSFilter(Long id);

    boolean start() throws Exception;

    boolean stop() throws Exception;

    boolean status() throws Exception;

    boolean deleteDnsFilter(String ids);

    boolean toggleDnsFilter(Long id, boolean enable);


}
