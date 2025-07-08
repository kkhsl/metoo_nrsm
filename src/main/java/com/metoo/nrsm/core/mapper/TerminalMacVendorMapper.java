package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalMacVendor;

import java.util.List;

/**
 * @author leo
 * @description 针对表【metoo_terminal_mac_vendor】的数据库操作Mapper
 * @createDate 2024-09-16 09:24:20
 * @Entity com.metoo.domain.TerminalMacVendor
 */
public interface TerminalMacVendorMapper {

    int deleteByVendor(String id);

    int insert(TerminalMacVendor record);

    TerminalMacVendor selectByVendor(String vendor);

    List<TerminalMacVendor> selectAllVendor(String vendor);

    int updateByMacVendor(TerminalMacVendor record);

    int updateByMacType(int oldType, int newType);

}
