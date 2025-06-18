package com.metoo.nrsm.core.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel("网关设备")
@Data
@NoArgsConstructor
public class UnitVO {

    private String unitName;
    private String department;
    private String area;
    private String city;

    private String date;
    private String time;

    private String vfourFlow;

    private String vsixFlow;

    private String broadband_Account;


    public UnitVO(String unitName, String department, String area, String city, String date, String time,
                  String vfourFlow, String vsixFlow, String broadband_Account) {
        this.unitName = unitName;
        this.department = department;
        this.area = area;
        this.city = city;
        this.date = date;
        this.time = time;
        this.vfourFlow = vfourFlow;
        this.vsixFlow = vsixFlow;
        this.broadband_Account = broadband_Account;
    }
}
