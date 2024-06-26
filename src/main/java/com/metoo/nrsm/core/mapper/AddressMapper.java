package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.Address;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AddressMapper {

    Address selectObjById(Long id);

    List<Address> selectObjByMap(Map map);

    Address selectObjByMac(String mac);

    Address selectObjByIp(String ip);

    int save(Address instance);

    int update(Address instance);

    int delete(Long id);

    void truncateTable();

}
