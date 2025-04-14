package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.config.annotation.excel.ExcelImport;
import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class NetworkElement extends IdEntity {

    @ExcelImport(value = "设备名称", unique = true, required = true)
    private String deviceName;
    @ExcelImport("ipv4管理地址")
    private String ip;
    @ExcelImport("ipv6管理地址")
    private String v6ip;

    private boolean isipv6;

    @ExcelImport("厂商")
    private String vendorName;
    @ExcelImport("类型")
    private String deviceTypeName;
    @ExcelImport("用途描述")
    private String description;

    private String filter;
    private String interfaceName;
    private Integer portIndex;
    private Long groupId;
    private String groupName;
    private Long deviceTypeId;
    private DeviceType deviceType;
    private Long vendorId;
    private Long userId;
    private String userName;
    private boolean sync_device;
    private String available; // 0 - (默认) 未知; 1 - 可用;2 - 不可用 3 。
    private String error;
    private String uuid;
    private String interfaceNames;
    private String flux;

    // 双栈助手登录
    @ApiModelProperty("连接类型 0：ssh 1：telnet")
    private Integer connectType;
    @ApiModelProperty("端口 22")
    private Integer port;
    @ApiModelProperty("凭据Id")
    private Long credentialId;
    @ApiModelProperty("凭据名称")
    private String credentialName;
    @ApiModelProperty("Web登录链接")
    private String webUrl;
    @ApiModelProperty("是否允许连接 默认 false：不允许 true：允许")
    private boolean permitConnect;


    // Excel 导入导出
    private Integer rowNum;
    private String rowData;
    private String rowTips;

    // 端口列表
    private List<Map> interfaces;

    // 附件列表
    private List<Accessory> configList = new ArrayList<>();

    // cpu
    private String cpu;
    // mem
    private String mem;
    // temp
    private String temp;

    @ExcelImport("SNMP community")
    private String community;
    @ApiModelProperty("SNMP版本")
    @ExcelImport("SNMP版本 version1: 0  version2c：1 version3：3")
    private String version;

    private String timeticks;

    @ApiModelProperty("默认：0 批量导入：2 ac：3")
    private Integer type;

    private List<Port> ports = new ArrayList<>();


    @ApiModelProperty("nswitch: 0：显示 1：隐藏")
    private boolean display = false;

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
