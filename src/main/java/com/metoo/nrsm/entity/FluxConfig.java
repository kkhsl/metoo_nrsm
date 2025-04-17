package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    private String ipv4;

    private String ipv6;

    private String community;

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

}
