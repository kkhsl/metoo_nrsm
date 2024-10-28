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

@ApiModel("")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AddressPool implements Serializable {

    private Long id;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date addTime;
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
    private Integer defaultLeaseTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String broadcast;


//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = "#" + name + " \n";
//    }
//
//    public String getSubnetAddresses() {
//        return subnetAddresses;
//    }
//
//    public void setSubnetAddresses(String subnetAddresses) {
//
//        if(MyStringUtils.isNotEmpty(subnetAddresses)){
//            if(subnetAddresses.contains("/")){
//                String ip = subnetAddresses.substring(0, subnetAddresses.indexOf("/"));
//                String mask = subnetAddresses.substring(subnetAddresses.indexOf("/") + 1);
//                this.subnetAddresses = "subnet " + ip + " netmask " + IpV4Util.getMaskByMaskBit(Integer.parseInt(mask)) + " { \n";
//            }
//        }else{
//            this.subnetAddresses = subnetAddresses;
//        }
//
//    }
//
//
//    public String getDefaultGateway() {
//        return defaultGateway;
//    }
//
//    public void setDefaultGateway(String defaultGateway) {
//        this.defaultGateway = "        option routers " + defaultGateway + ";\n";
//    }
//
//    public String getAddressPoolRange() {
//        return addressPoolRange;
//    }
//
//    public void setAddressPoolRange(String addressPoolRange) {
//        this.addressPoolRange = addressPoolRange;
//    }
//
//    public String getDNS() {
//        return DNS;
//    }
//
//    public void setDNS(String DNS) {
//        this.DNS = "        option domain-name-servers " + DNS + ";\n";
//    }
//
//    public String getBroadcastAddress() {
//        return broadcastAddress;
//    }
//
//    public void setBroadcastAddress(String broadcastAddress) {
//        this.broadcastAddress = broadcastAddress;
//    }
//
//    public String getLeaseTime() {
//        return leaseTime;
//    }
//
//    public void setLeaseTime(String leaseTime) {
//        this.leaseTime = leaseTime;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Date getAddTime() {
//        return addTime;
//    }
//
//    public void setAddTime(Date addTime) {
//        this.addTime = addTime;
//    }
//
//    public Integer getDefaultLeaseTime() {
//        return defaultLeaseTime;
//    }
//
//    public void setDefaultLeaseTime(Integer defaultLeaseTime) {
//        this.defaultLeaseTime = defaultLeaseTime;
//    }
//
//    public String getBroadcast() {
//        return broadcast;
//    }
//
//    public void setBroadcast(String broadcast) {
//        this.broadcast = broadcast;
//    }


}
