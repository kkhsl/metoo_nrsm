package com.metoo.nrsm.core.service.impl;


import com.metoo.nrsm.core.mapper.TerminalMacIpv6Mapper;
import com.metoo.nrsm.core.service.ITerminalMacIpv6Service;
import com.metoo.nrsm.entity.TerminalMacIpv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author leo
 * @description 针对表【metoo_mac】的数据库操作Service实现
 * @createDate 2024-09-14 14:45:58
 */
@Service
public class TerminalMacIpv6ServiceImpl implements ITerminalMacIpv6Service {

    @Autowired
    private TerminalMacIpv6Mapper metooMacMapper;

    @Override
    public int insertMac(String mac, int isIPv6) {
        return metooMacMapper.insertMac(mac, isIPv6);
    }

    public List<TerminalMacIpv6> getAllMacs() {
        return metooMacMapper.getAllMacs();
    }

    @Override
    public TerminalMacIpv6 getMacByMacAddress(String mac) {
        return metooMacMapper.getMacByMacAddress(mac);
    }

    public void updateMac(String mac, int isIPv6) {
        metooMacMapper.updateMac(mac, isIPv6);
    }

    public void deleteMac(String mac) {
        metooMacMapper.deleteMac(mac);
    }
}




