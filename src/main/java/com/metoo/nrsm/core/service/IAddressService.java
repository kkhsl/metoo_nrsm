package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.Address;

import java.util.List;
import java.util.Map;

public interface IAddressService {

    Address selectObjById(Long id);

    Address selectObjByIp(String ip);

    Address selectObjByMac(String mac);

    List<Address> selectObjByMap(Map map);

    int save(Address instance);

    int update(Address instance);

    int delete(Long id);

    void truncateTable();

}
