package com.metoo.nrsm.entity;

import com.alibaba.fastjson.JSON;
import com.metoo.nrsm.core.domain.IdEntity;
import com.metoo.nrsm.core.utils.string.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 10:41
 */
@ApiModel("流量配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FluxConfig extends IdEntity {

    private String name;

    //    private String ipv4;
//    private String ipv6;
    @ApiModelProperty("设备IP列表")
    private String ips;

    private String community;

    private int port;

    @ApiModelProperty("SNMP版本")
    private String version;

    private String ipv4Oid;

    private List<List<String>> ipv4Oids = new ArrayList<>();

    private String ipv6Oid;

    private List<List<String>> ipv6Oids = new ArrayList<>();

    private Integer update;

    // snmpv3
    // 安全模型
    // noAuthNoPriv：无认证和加密，最不安全的模式
    // authNoPriv：仅认证，但不加密
    // authPriv：认证并加密，是最安全的模式
    @ApiModelProperty("用户名")
    private String securityName;
    @ApiModelProperty("安全等级 1 2 3")
    private Integer securityLevel;
    @ApiModelProperty("认证协议 MD5/SHA")
    private String authProtocol;
    @ApiModelProperty("认证密码")
    private String authPassword;
    @ApiModelProperty("加密协议 DES/AES")
    private String privProtocol;
    @ApiModelProperty("加密密码")
    private String privPassword;

    public List<String> getAllIpList() {
        return parseIpList(this.ips);
    }

    public List<String> getIpList() {
        List<String> rawList = parseIpList(this.ips);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> cleanedList = new ArrayList<>();
        for (String address : rawList) {
            // 去除可能存在的端口号
            String cleanIp = removePortNumber(address);
            if (StringUtils.isNotBlank(cleanIp)) {
                cleanedList.add(cleanIp);
            }
        }
        return cleanedList;
    }

    private String removePortNumber(String address) {
        if (address == null) {
            return null;
        }

        // IPv6 可能包含方括号的情况：如 [::1]:8080
        if (address.startsWith("[")) {
            int bracketEnd = address.indexOf("]");
            if (bracketEnd != -1) {
                return address.substring(1, bracketEnd);
            }
        }

        // IPv4 或没有方括号的 IPv6 处理
        int colonIndex = address.indexOf("/");
        if (colonIndex == -1) {
            return address; // 没有端口号
        }

        // 检查端口部分是否是纯数字（防止误切 IPv6 地址）
        String portPart = address.substring(colonIndex + 1);
        if (portPart.matches("\\d+")) {
            return address.substring(0, colonIndex);
        }

        // 不是端口号，可能是 IPv6 地址中的冒号
        return address;
    }


    private List<String> parseIpList(String ips) {
        if (ips == null || ips.isEmpty()) {
            return Collections.emptyList();
        }

        // 尝试解析为 JSON 数组
        try {
            if (ips.startsWith("[")) {
                return JSON.parseArray(ips, String.class);
            }
        } catch (Exception e) {
            // 解析失败，可能不是 JSON 格式
        }

        // 尝试按逗号分割
        if (ips.contains(",")) {
            return Arrays.asList(ips.split("\\s*,\\s*"));
        }

        // 否则作为单个 IP
        return Collections.singletonList(ips);
    }

}
