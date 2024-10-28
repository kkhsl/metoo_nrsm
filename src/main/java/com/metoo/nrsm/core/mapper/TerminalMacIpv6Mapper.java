package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.entity.TerminalMacIpv6;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TerminalMacIpv6Mapper {

    @Insert("INSERT INTO metoo_terminal_mac_v6(mac, isIPv6) VALUES(#{mac}, #{isIPv6})")
    int insertMac(@Param("mac") String mac, @Param("isIPv6") int isIPv6);

    @Select("SELECT * FROM metoo_terminal_mac_v6")
    List<TerminalMacIpv6> getAllMacs();

    @Select("SELECT * FROM metoo_terminal_mac_v6 WHERE mac = #{mac}")
    TerminalMacIpv6 getMacByMacAddress(String mac);

    @Update("UPDATE metoo_terminal_mac_v6 SET isIPv6 = #{isIPv6} WHERE mac = #{mac}")
    void updateMac(String mac,int isIPv6);

    @Delete("DELETE FROM metoo_terminal_mac_v6 WHERE mac = #{mac}")
    void deleteMac(String mac);

}
