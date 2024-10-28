package com.metoo.nrsm.core.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class AddressPoolIpv6VO implements Serializable {

    @ApiModelProperty("租约时间：单位：秒 默认值：2592000")
    private String defaultLeaseTime;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(StringUtils.isNotEmpty(name)){
            name = name.replaceAll("\\s*|\r|\n|\t","");
            this.name = "#" + name + " \n";
        }else{
            this.name =  name;
        }
    }


    public String getSubnetAddresses() {
        return subnetAddresses;
    }

    public void setSubnetAddresses(String subnetAddresses) {
        if(StringUtils.isNotEmpty(subnetAddresses)){
            if(subnetAddresses.contains("/")){
                subnetAddresses = subnetAddresses.replaceAll("\\s*|\r|\n|\t","");
                this.subnetAddresses = "subnet6 " + subnetAddresses + " { \n";
            }
        }else{
            this.subnetAddresses = subnetAddresses;
        }
    }


    public String getAddressPoolRange() {
        return addressPoolRange;
    }

    public void setAddressPoolRange(String addressPoolRange) {
        if(StringUtils.isNotEmpty(addressPoolRange)){
            addressPoolRange = addressPoolRange.replaceAll("\r|\n|\t","");
            addressPoolRange = addressPoolRange.replaceAll("-", " ");
            this.addressPoolRange = "        range6 " + addressPoolRange + ";\n";
        }else{
            this.addressPoolRange =  addressPoolRange;
        }
    }

    public String getDNS() {
        return DNS;
    }

    public void setDNS(String DNS) {
        if(StringUtils.isNotEmpty(DNS)){
//            DNS = DNS.replaceAll("\\s*|\r|\n|\t","");
//            this.DNS = "        option dhcp6.name-servers " + DNS + ";\n";
            // 解析dns
            try {
                List<String> dns = MyStringUtils.str2list(DNS);
                if(dns.size() >= 0){
                    if(dns.size() == 1){
                        this.DNS = "        option dhcp6.name-servers " + DNS + ";\n";
                    }else{
                        StringBuffer sb = new StringBuffer();
                        for (String str : dns) {
                            sb.append(str).append(",");
                        }
                        this.DNS = "        option dhcp6.name-servers " + sb.substring(0, sb.toString().lastIndexOf(",")) + ";\n";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            this.DNS =  DNS;
        }
    }


    public void getDefault(){
         {
            this.defaultLeaseTime = "default-lease-time 2592000; \n";
            this.preferred_lifetime = "preferred-lifetime 604800;\n";
            this.dhcp_renewal_time = "option dhcp-renewal-time 3600;\n";
            this.option_dhcp_rebinding_time = "option dhcp-rebinding-time 7200;\n";
            this.allow_leasequery = "allow leasequery;\n";
            this.option_dhcp6_info_refresh_time = "option dhcp6.info-refresh-time 21600;\n";
        }
     }
    // 初始化块为属性设置默认值
//    {
//        this.defaultLeaseTime = "default-lease-time 2592000; \n";
//        this.preferred_lifetime = "preferred-lifetime 604800;\n";
//        this.dhcp_renewal_time = "option dhcp-renewal-time 3600;\n";
//        this.option_dhcp_rebinding_time = "option dhcp-rebinding-time 7200;\n";
//        this.allow_leasequery = "allow leasequery;\n";
//        this.option_dhcp6_info_refresh_time = "option dhcp6.info-refresh-time 21600;\n";
//    }
}

