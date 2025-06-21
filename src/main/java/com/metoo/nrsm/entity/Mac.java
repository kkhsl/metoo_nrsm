package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
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
public class Mac extends IdEntity {

    private String mac;
    private String mac1;
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



    @ApiModelProperty("终端Uuid")
    private String uuid;

    private String status;

    @ApiModelProperty("设备名称")
    private String deviceIp;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("设备Uuid")
    private String deviceUuid;
    private boolean deviceDisplay;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("标记")
    private String tag;

    private String mark;

    private String vendor;

    private String hostname;

    private String remoteDevice;
    private String remotePort;
    private String remoteIp;
    private String remoteDeviceUuid;

    private String remoteDeviceIp;
    private String remoteDeviceName;

    private String remoteDevicTypeeUuid;
    private String remoteDeviceType;


    private String deviceTypeUuid;

    private List<Long> macList = new ArrayList();

    private String client_hostname;


    private String deviceIp2;
    private String deviceName2;
    private String devicePort2;

}
