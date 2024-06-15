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
public class AddressPoolFixed implements Serializable {

    private Long id;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date addTime;
    @ApiModelProperty("")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host;
    @ApiModelProperty("hardware ethernet 58:20:59:8b:f4:94")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hardware_ethernet;
    @ApiModelProperty("fixed-address 192.168.5.5")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fixed_address;

}
