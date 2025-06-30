package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-25 22:30
 *
 * 双缓冲方案（高可用方案）避免读取和写入出现空窗期
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Probe {


    @ApiModelProperty("Id")
    private Integer id;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("目标IP")
    private String ip_addr;
    @ApiModelProperty("目标Port")
    private String port_num;
    @ApiModelProperty("目标mac")
    private String mac_addr;
    @ApiModelProperty("识别设备制造商")
    private String vendor;
    @ApiModelProperty("操作系统类型")
    private String os_family;
    @ApiModelProperty("操作系统代际")
    private String os_gen;
    @ApiModelProperty("操作系统cpe信息")
    private String os_cpe;
    @ApiModelProperty("扫描时间")
    private String scan_time;
    @ApiModelProperty("更新扫描时间")
    private String update_time;
    @ApiModelProperty("TCP或UDP")
    private String communicate_protocol;
    @ApiModelProperty("如http、telnet")
    private String application_protocol;
    @ApiModelProperty("探测目标响应数据")
    private String service_output;
    @ApiModelProperty("服务扫描cpe信息")
    private String port_service_cpe;
    @ApiModelProperty("产品信息")
    private String port_service_product;
    @ApiModelProperty("产品版本")
    private String port_service_version;
    @ApiModelProperty("产品厂商")
    private String port_service_vendor;
    @ApiModelProperty("设备类型")
    private String device_type;
    @ApiModelProperty("Web爬虫获取网站标题")
    private String title;
    @ApiModelProperty("Web爬虫获取网站响应数据")
    private String response;
    @ApiModelProperty("匹配的指纹")
    private String fingerId;
    private String fingerIdOsScan;
    @ApiModelProperty("ttl值")
    private Integer ttl;

    @ApiModelProperty("用于逻辑判断")
    private String ttls;

    @ApiModelProperty("os可用率")
    private Float reliability;
    @ApiModelProperty("ipv6")
    private String ipv6;
    @ApiModelProperty("mac地址")
    private String mac;
    private String mac_vendor;
    public Probe(String port_num) {
        this.port_num = port_num;
    }

    @ApiModelProperty("port_num + application_protocol")
    private String combined;

    @ApiModelProperty("vendor + os_family + os_gen")
    private String combined_os;

    @ApiModelProperty("ttl")
    private String combined_ttl;
    @ApiModelProperty("vendor")
    private String combined_vendor;
    @ApiModelProperty("application_protocol")
    private String combined_application_protocol;
}
