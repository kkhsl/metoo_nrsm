package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Subnet;

import java.util.List;
import java.util.Map;

public interface ISubnetService {

    Subnet selectObjById(Long id);

    Subnet selectObjByIp(String ip);

    Subnet selectObjByIpAndMask(String ip, Integer mask);

    List<Subnet> selectSubnetByParentId(Long id);

    List<Subnet> selectSubnetByParentIp(Long ip);

    List<Subnet> selectObjByMap(Map params);

    List<Subnet> leafIpSubnetMapper(Map params);

    int save(Subnet instance);

    Result update(Subnet instance);

    int delete(Long id);

    int deleteTable();

    Result comb();

    void pingSubnet();

}
