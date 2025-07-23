package com.metoo.nrsm.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
public class AreaVO {

    private String unitId;
    private String city;
    private String area;
    private String unit;
    private String date;

    public AreaVO(String unitId, String city, String area, String unit, String date) {
        this.unitId = unitId;
        this.city = city;
        this.area = area;
        this.unit = unit;
        this.date = date;
    }

}
