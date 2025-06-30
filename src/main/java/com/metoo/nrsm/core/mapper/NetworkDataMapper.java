package com.metoo.nrsm.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface NetworkDataMapper {

    /**
     * 获取IPv6端口表
     * @return List<Map<String, String>> 交换机端口、网段地址和网络掩码的列表
     */
    @Select("SELECT port as \"交换机端口\", ip as \"网段地址\", mask as \"网络掩码\" " +
            "FROM metoo_port WHERE status = '1'")
    List<Map<String, String>> getIpv6PortTable();


    /**
     * 获取终端表
     * @return List<Map<String, String>> 包含终端IPv4和IPv6地址的列表
     */
    @Select("SELECT v4ip as \"ipv4地址\", " +
            "v6ip as \"ipv6地址\", " +
            "v6ip1 as \"ipv6地址1\", " +
            "v6ip2 as \"ipv6地址2\", " +
            "v6ip3 as \"ipv6地址3\" " +
            "FROM metoo_terminal " +
            "WHERE online = true AND v4ip IS NOT NULL")
    List<Map<String, String>> getTerminalTable();

    /**
     * 获取IPv6连接性状态
     * @return List<Map<String, String>> IPv6是否ping通的状态
     */
    @Select("SELECT CASE WHEN v6isok = '1' THEN '通' ELSE '不通' END as \"是否ping通\" " +
            "FROM metoo_ping " +
            "ORDER BY uptime DESC LIMIT 1")
    List<Map<String, String>> getIpv6Connectivity();

    /**
     * 获取端口表
     * @return List<Map<String, String>> 交换机端口和IPv6地址的映射
     */
    @Select("SELECT port as \"交换机端口\", ipv6 as \"ipv6地址\" " +
            "FROM metoo_port_ipv6")
    List<Map<String, String>> getPortTable();
}