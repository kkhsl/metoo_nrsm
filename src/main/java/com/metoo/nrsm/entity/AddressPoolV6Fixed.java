package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@ApiModel("固定地址池")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AddressPoolV6Fixed implements Serializable {

    private Long id;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date addTime;

    @ApiModelProperty("")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host;
    @ApiModelProperty("host-identifier option dhcp6.client-id 00:01:00:01:4a:1f:ba:e3:60:b9:1f:01:23:45")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host_identifier_option_dhcp6_client_id;
    @ApiModelProperty("fixed-address6 2001:db8:0:1::127")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fixed_address6;
//    private String subnet;

}
