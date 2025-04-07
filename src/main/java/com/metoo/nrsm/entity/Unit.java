package com.metoo.nrsm.entity;


import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel("网关设备")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Unit extends IdEntity {

    private String department;
    private String area;
    private String city;
    private String vlanNum;
    private Date date;
    private String time;
    private Long gatewayId;
    private String gatewayName;
    private String pattern;
    private String rule;
    private String unitName;
    private boolean hidden;
    private String vfourFlow;
    private String vsixFlow;
    private String broadband_Account;
    private String random;
    private String v4Traffic;
    private Long unitId;

}
