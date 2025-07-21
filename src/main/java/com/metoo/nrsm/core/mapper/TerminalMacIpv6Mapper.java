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

    /**
     * 批量通过MAC地址查询记录
     *
     * @param macs MAC地址列表
     * @return 匹配的记录列表
     */
    List<TerminalMacIpv6> batchGetByMacAddresses(@Param("macs") List<String> macs);

    List<TerminalMacIpv6> findAllExcludingMacs(@Param("excludedMacs") List<String> excludedMacs);

    @Update("UPDATE metoo_terminal_mac_v6 SET isIPv6 = #{isIPv6} WHERE mac = #{mac}")
    void updateMac(@Param("mac") String mac, @Param("isIPv6") int isIPv6);

    @Delete("DELETE FROM metoo_terminal_mac_v6 WHERE mac = #{mac}")
    void deleteMac(String mac);

}
