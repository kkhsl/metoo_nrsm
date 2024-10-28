package com.metoo.nrsm.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

@ApiModel("")
public class AddressPoolFixedVO implements Serializable {


    @ApiModelProperty("")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String annotation;

    @ApiModelProperty("")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host;
    @ApiModelProperty("hardware ethernet 58:20:59:8b:f4:94")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hardware_ethernet;
    @ApiModelProperty("fixed-address 192.168.5.5")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fixed_address;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
//        if(MyStringUtils.isNotEmpty(host)){
//            host = host.replaceAll("\\s*|\r|\n|\t","");
//            this.host = "host " + " {\n";
//        }else{
//            this.host =  host;
//        }
        this.host =  host;
        this.annotation =  "# " + host + "\n";;
    }

    public String getHardware_ethernet() {
        return hardware_ethernet;
    }

    public void setHardware_ethernet(String hardware_ethernet) {
        if(StringUtils.isNotEmpty(hardware_ethernet)){
            hardware_ethernet = hardware_ethernet.replaceAll("\\s*|\r|\n|\t","");
            this.hardware_ethernet = "        hardware ethernet " + hardware_ethernet + ";\n";
        }else{
            this.hardware_ethernet =  hardware_ethernet;
        }
    }

    public String getFixed_address() {
        return fixed_address;
    }

    public void setFixed_address(String fixed_address) {
        if(StringUtils.isNotEmpty(fixed_address)){
            fixed_address = fixed_address.replaceAll("\\s*|\r|\n|\t","");
            this.fixed_address = "        fixed-address " + fixed_address + ";\n";
            if(StringUtils.isNotEmpty(host)){
                host = host.replaceAll("\\s*|\r|\n|\t","");
                this.host = "host " + fixed_address + "{\n";
            }
        }else{
            this.fixed_address =  fixed_address;
        }
    }
}
