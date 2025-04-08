package com.metoo.nrsm.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel("拓扑终端")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Terminal extends IdEntity {

    @ApiModelProperty("采集时间")
    private Date time;

    private String name;
    private String mac;
    private String port;

//    @ApiModelProperty("类型 0: 采集终端 1：资产终端 3 从外部学习到的mac")
//    private String type;
//
    @ApiModelProperty("类型 0: 普通终端 1：资产终端")
    private Integer type;

    @ApiModelProperty("类型 0: 网元列表里的ip在terminal里面的devicetype设置为0 " +
            " 1：设备" +
            " 2：vmware " +
            " 3：nswitch ")
    private Integer deviceType;

    private String macVendor;

    private String v4ip;
    private String v4ip1;
    private String v4ip2;
    private String v4ip3;

    private String v4ipDynamic;
    private String v4ip1Dynamic;
    private String v4ip2Dynamic;
    private String v4ip3Dynamic;

    private String v6ip;
    private String v6ip1;
    private String v6ip2;
    private String v6ip3;

    private String v6ipDynamic;
    private String v6ip1Dynamic;
    private String v6ip2Dynamic;
    private String v6ip3Dynamic;

    private String status;

    @ApiModelProperty("设备名称")
    private String deviceIp;
    @ApiModelProperty("设备名称")
    private String deviceName;

    private String deviceUuid;

    private String deviceUuid2;
    private String deviceName2;
    private String deviceIp2;
    private String devicePort2;

    @ApiModelProperty("设备类型")
    private Long deviceTypeId;
    private String deviceTypeName;
    private String deviceTypeUuid;

    @ApiModelProperty("标记")
    private String tag;

    private String mark;

    private String hostname;

    private String remoteDevice;
    private String remotePort;

    private String remoteDeviceIp;
    private String remoteDeviceName;
    private String remoteDeviceUuid;
    private String remoteDevicTypeeUuid;
    private String remoteDeviceType;


    private String client_hostname;

    @ApiModelProperty("是否在线 默认0：离线 1：在线\"")
    private Boolean online;


    @ApiModelProperty("接口名称")
    private String interfaceName;
    @ApiModelProperty("Mac索引")
    private String index;
    @ApiModelProperty("索引")
    private String uuid;
    @ApiModelProperty("接口索引")
    private String interfaceIndex;

    @ApiModelProperty("端口状态")
    private Integer interfaceStatus;

    @ApiModelProperty("对端接口名称")
    private String remoteInterface;
    private String remoteUuid;
    private String vendor;
    private String vlan;
    private Long terminalTypeId;
    private String terminalTypeName;

    @ApiModelProperty("部门Id")
    private Long departmentId;
    @ApiModelProperty("部门名称")
    private String departmentName;

    @ApiModelProperty("责任人")
    private String duty;
    @ApiModelProperty("设备位置：摄像头等")
    private String location;


    @ApiModelProperty("采购时间")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date purchase_time;

    @ApiModelProperty("过保时间")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date warranty_time;

    @JSONField(name="price",serializeUsing = DoubleSerializer.class)
    @ApiModelProperty("价格")
    private Double price;

    @ApiModelProperty("序列号")
    private String serial_number;

    @ApiModelProperty("资产编号")
    private String asset_number;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty(value = "变更原因")
    private String changeReasons;

    @ApiModelProperty("来源 0：采集 1：手动录入 2:  3:HUB-terminal（已手动修改终端，采集不在更新设备）")
    private Integer from;

    @ApiModelProperty("项目Id")
    private Long projectId;
    @ApiModelProperty("项目名")
    private String projectName;
    @ApiModelProperty("厂商ID")
    private Long vendorId;
    @ApiModelProperty("厂商名称")
    private String vendorName;
    @ApiModelProperty("型号")
    private String model;

    private List<Integer> count = new ArrayList<>();

    private Integer v4ip_count;
    private Integer v6ip_count;
    private Integer v4ip_v6ip_count;

    private String unitName;
    private Long unitId;
    private Integer isIpv6;

    private String scan_port_number;
    private String scan_vendor;
    private String scan_os_famify;
    private String scan_os_gen;
    private String scan_application_protocol;


    @ApiModelProperty("操作系统 windows，linux，android，apple，vmware")
    private String os;
    @ApiModelProperty("port + protocol")
    private String combined_port_protocol;
    @ApiModelProperty("vendor + gen + family")
    private String combined_vendor_gen_family;


    private List<Terminal> terminalList = new ArrayList<>();



}
