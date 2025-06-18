package com.metoo.nrsm.core.service;


import com.metoo.nrsm.core.mapper.TerminalMacIpv6Mapper;
import com.metoo.nrsm.entity.TerminalMacIpv6;

import java.util.List;

/**
* @author leo
* @description 针对表【metoo_mac】的数据库操作Service
* @createDate 2024-09-14 14:45:58
*/
public interface ITerminalMacIpv6Service {

    int insertMac(String mac, int isIPv6);

    List<TerminalMacIpv6> getAllMacs();

    TerminalMacIpv6 getMacByMacAddress(String mac);

    void updateMac(String mac, int isIPv6);

    void deleteMac(String mac);
}
