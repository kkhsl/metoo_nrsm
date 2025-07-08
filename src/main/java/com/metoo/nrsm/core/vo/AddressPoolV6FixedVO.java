package com.metoo.nrsm.core.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@ApiModel("")
public class AddressPoolV6FixedVO implements Serializable {


    @ApiModelProperty("")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String annotation;

    @ApiModelProperty("")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host;
    @ApiModelProperty("host-identifier option dhcp6.client-id 00:01:00:01:4a:1f:ba:e3:60:b9:1f:01:23:45")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host_identifier_option_dhcp6_client_id;
    @ApiModelProperty("fixed-address6 2001:db8:0:1::127")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fixed_address6;

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
        this.host = host;
        this.annotation = "# " + host + "\n";
        ;
    }

    public String getHost_identifier_option_dhcp6_client_id() {
        return host_identifier_option_dhcp6_client_id;
    }

    public void setHost_identifier_option_dhcp6_client_id(String host_identifier_option_dhcp6_client_id) {
        if (StringUtils.isNotEmpty(host_identifier_option_dhcp6_client_id)) {
            host_identifier_option_dhcp6_client_id = host_identifier_option_dhcp6_client_id.replaceAll("\\s*|\r|\n|\t", "");
            this.host_identifier_option_dhcp6_client_id = "        host-identifier option dhcp6.client-id " + host_identifier_option_dhcp6_client_id + ";\n";
        } else {
            this.host_identifier_option_dhcp6_client_id = host_identifier_option_dhcp6_client_id;
        }
    }

    public String getFixed_address6() {
        return fixed_address6;
    }

    public void setFixed_address6(String fixed_address6) {
        if (StringUtils.isNotEmpty(fixed_address6)) {
            fixed_address6 = fixed_address6.replaceAll("\\s*|\r|\n|\t", "");
            this.fixed_address6 = "        fixed-address6 " + fixed_address6 + ";\n";
//            if(MyStringUtils.isNotEmpty(host)){
//                host = host.replaceAll("\\s*|\r|\n|\t","");
//                this.host = "host " + fixed_address6 + "{\n";
//            }
        } else {
            this.fixed_address6 = fixed_address6;
        }
    }

    public AddressPoolV6FixedVO() {
    }

    public AddressPoolV6FixedVO(String host, int host_index, String host_identifier_option_dhcp6_client_id,
                                String fixed_address6) {

        this.host = "host " + host_index + " {\n";

        this.annotation = "# " + host + "\n";

        if (StringUtils.isNotEmpty(host_identifier_option_dhcp6_client_id)) {
            host_identifier_option_dhcp6_client_id = host_identifier_option_dhcp6_client_id.replaceAll("\\s*|\r|\n|\t", "");
            this.host_identifier_option_dhcp6_client_id = "        host-identifier option dhcp6.client-id " + host_identifier_option_dhcp6_client_id + ";\n";
        } else {
            this.host_identifier_option_dhcp6_client_id = host_identifier_option_dhcp6_client_id;
        }

        if (StringUtils.isNotEmpty(fixed_address6)) {
            fixed_address6 = fixed_address6.replaceAll("\\s*|\r|\n|\t", "");
            this.fixed_address6 = "        fixed-address6 " + fixed_address6 + ";\n";
//            if(MyStringUtils.isNotEmpty(host)){
//                host = host.replaceAll("\\s*|\r|\n|\t","");
//                this.host = "host " + fixed_address6 + "{\n";
//            }
        } else {
            this.fixed_address6 = fixed_address6;
        }
    }
}
