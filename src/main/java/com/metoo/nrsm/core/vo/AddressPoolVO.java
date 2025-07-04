package com.metoo.nrsm.core.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ApiModel("")
public class AddressPoolVO implements Serializable {

    @ApiModelProperty("名称")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @ApiModelProperty("subnet 192.168.5.0 netmask 255.255.255.0")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subnetAddresses;
    @ApiModelProperty("option routers")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String defaultGateway;
    @ApiModelProperty("range")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String addressPoolRange;
    @ApiModelProperty("option domain-name-servers")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String DNS;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String broadcastAddress;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String leaseTime;
    @ApiModelProperty("租约时间：单位：秒 默认值：7200")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String defaultLeaseTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String broadcast;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (StringUtils.isNotEmpty(name)) {
            name = name.replaceAll("\\s*|\r|\n|\t", "");
            this.name = "#" + name + " \n";
        } else {
            this.name = name;
        }
    }

    public String getSubnetAddresses() {
        return subnetAddresses;
    }

    public void setSubnetAddresses(String subnetAddresses) {

        if (StringUtils.isNotEmpty(subnetAddresses)) {
            if (subnetAddresses.contains("/")) {
                subnetAddresses = subnetAddresses.replaceAll("\\s*|\r|\n|\t", "");
                String ip = subnetAddresses.substring(0, subnetAddresses.indexOf("/"));
                String mask = subnetAddresses.substring(subnetAddresses.indexOf("/") + 1);
                this.subnetAddresses = "subnet " + ip + " netmask " + Ipv4Util.getMaskByMaskBit(Integer.parseInt(mask)) + " { \n";
            }
        } else {
            this.subnetAddresses = subnetAddresses;
        }

    }


    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(String defaultGateway) {
        if (StringUtils.isNotEmpty(defaultGateway)) {
            defaultGateway = defaultGateway.replaceAll("\\s*|\r|\n|\t", "");
            this.defaultGateway = "        option routers " + defaultGateway + ";\n";
        } else {
            this.defaultGateway = defaultGateway;
        }
    }

    public String getAddressPoolRange() {
        return addressPoolRange;
    }

    public void setAddressPoolRange(String addressPoolRange) {
        if (StringUtils.isNotEmpty(addressPoolRange)) {
            addressPoolRange = addressPoolRange.replaceAll("\r|\n|\t", "");
            addressPoolRange = addressPoolRange.replaceAll("-", " ");
            this.addressPoolRange = "        range " + addressPoolRange + ";\n";
        } else {
            this.addressPoolRange = addressPoolRange;
        }
    }

    public String getDNS() {
        return DNS;
    }

    public void setDNS(String DNS) {
        if (StringUtils.isNotEmpty(DNS)) {
//            DNS = DNS.replaceAll("\\s*|\r|\n|\t","");
//            this.DNS = "        option domain-name-servers " + DNS + ";\n";

            // 前端dns 按行显示
//            DNS = DNS.replaceAll("\\s*|\r|\t","");
            // 解析dns
            try {
                List<String> dns = MyStringUtils.str2list(DNS);
                if (dns.size() >= 0) {
                    if (dns.size() == 1) {
                        this.DNS = "        option domain-name-servers " + DNS + ";\n";
                    } else {
                        StringBuffer sb = new StringBuffer();
                        for (String str : dns) {
                            sb.append(str).append(",");
                        }
                        this.DNS = "        option domain-name-servers " + sb.substring(0, sb.toString().lastIndexOf(",")) + ";\n";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            this.DNS = DNS;
        }
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public String getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(String leaseTime) {
        this.leaseTime = leaseTime;
    }

    public String getDefaultLeaseTime() {
        return defaultLeaseTime;
    }

    public void setDefaultLeaseTime(String defaultLeaseTime) {
        if (StringUtils.isNotEmpty(defaultLeaseTime)) {
            defaultLeaseTime = defaultLeaseTime.replaceAll("\\s*|\r|\n|\t", "");
            this.defaultLeaseTime = "        default-lease-time " + defaultLeaseTime + ";\n";
        } else {
            this.defaultLeaseTime = defaultLeaseTime;
        }
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }


}
