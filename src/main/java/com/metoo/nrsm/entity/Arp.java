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
 * @date 2024-02-01 11:57
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Arp extends IdEntity {

    private String v4ip;
    private String v6ip;
    private String mac;
    private String port;

    private String v4ip1;
    private String v4ip2;
    private String v4ip3;

    private String v6ip1;
    private String v6ip2;
    private String v6ip3;


    @ApiModelProperty("设备名称")
    private String deviceIp;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("标记")
    private String tag;

    @ApiModelProperty("厂商")
    private String macVendor;

    List<Ipv6> ipv6List = new ArrayList();


}
