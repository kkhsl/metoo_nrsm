package com.metoo.nrsm.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Mac;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 17:15
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MacDTO extends PageDto<Mac> {

    private String filter;

    private String mac;
    private String port;
    private String type;// 3 从外部学习到的mac

    private String macVendor;

    private String v4ip;
    private String v4ip1;
    private String v4ip2;
    private String v4ip3;
    private String v4ipAll;

    private String v4ipDynamic;
    private String v4ip1Dynamic;
    private String v4ip2Dynamic;
    private String v4ip3Dynamic;

    private String v6ip;
    private String v6ip1;
    private String v6ip2;
    private String v6ip3;
    private String v6ipAll;

    private String v6ipDynamic;
    private String v6ip1Dynamic;
    private String v6ip2Dynamic;
    private String v6ip3Dynamic;


    private String status;

    @ApiModelProperty("设备名称")
    private String deviceIp;
    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("设备类型")
    private String deviceType;

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


    private String deviceUuid;

    private String deviceTypeUuid;

    private List<Long> macList = new ArrayList();

    private String client_hostname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
}
