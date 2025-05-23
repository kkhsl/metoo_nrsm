package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@ApiModel("配置 DNS 解析器")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Unbound extends IdEntity {

    @ApiModelProperty("设置私有地址范围的过滤。::/0 表示所有的 IPv6 地址都视为私有地址，关闭外部向内部网络发送 IPv6 请求。")
    private Boolean privateAddress;

    @ApiModelProperty("转发地址：上游 DNS 服务器地址")
    private String forwardAddress;
    @ApiModelProperty("主机名：需要解析的域名")
    private String hostName;
    @ApiModelProperty("记录类型 可选 A, AAAA, CNAME 等")
    private String recordType;
    @ApiModelProperty("映射地址：域名对应的 IP 地址")
    private String mappedAddress;
    @ApiModelProperty("定义 DNS 区域名称，通常是一个域名")
    private String zoneName;
    @ApiModelProperty("区域类型，如 static、redirect")
    private String zoneType; // 行为类型

    private String localData;

    private String localZone;

    private Set<String> interfaces = new HashSet();

//    private String forwardAddress = "223.5.5.5";
//    private String hostName = "www.szzs.com.";
//    private String recordType = "A";
//    private String mappedAddress = "192.168.5.205";
//    private String zoneName = "szzs.com.";
//    private String zoneType = "static";

    // 拼接 local-data
//    String localData = String.format("%s IN %s %s", hostName, recordType, mappedAddress);

//    String localZone = String.format("local-zone: \"%s\" %s", zoneName, zoneType);
}
