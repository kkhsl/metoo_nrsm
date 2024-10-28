package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@ApiModel("Ipv6地址池")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressPoolIpv6 implements Serializable {

    private Long id;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date addTime;
    @ApiModelProperty("租约时间：单位：秒 默认值：2592000")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String defaultLeaseTime;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String preferred_lifetime;
    private String dhcp_renewal_time;
    private String option_dhcp_rebinding_time;
    private String allow_leasequery;
    private String option_dhcp6_info_refresh_time;

    @ApiModelProperty("名称")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @ApiModelProperty("子网地址: subnet6 240e:380:11d:2::/64")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subnetAddresses;
    @ApiModelProperty("range6 240e:380:11d:2::667 240e:380:11d:2::ffff")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String addressPoolRange;
    @ApiModelProperty("DNS服务器地址: option dhcp6.name-servers")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String DNS;

}
