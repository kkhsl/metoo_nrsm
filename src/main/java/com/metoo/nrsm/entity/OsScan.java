package com.metoo.nrsm.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class OsScan {

    private Integer id;
    private Date addTime;
    private Date createTime;
    private String IP;
    private String openPort;
    private String closePort;
    private String DstOrGW_MAC;
    private String manufacturer;
    private String ttl;
    private String FingerID;
    private String Reliability;
    private String OsVendor;
    private String OsGen;
    private String OsFamily;

}
