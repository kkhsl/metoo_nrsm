package com.metoo.nrsm.entity;


import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Unit extends IdEntity {
    private String cityCode;     // 市级编码
    private String cityName;     // 市级名称
    private String countyCode;   // 区县级编码
    private String countyName;   // 区县级名称
    private String unitName;
    private String unitId;
    private Integer unitLevel;
}
