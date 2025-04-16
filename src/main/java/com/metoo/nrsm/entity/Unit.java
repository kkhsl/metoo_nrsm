package com.metoo.nrsm.entity;


import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("网关设备")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Unit extends IdEntity {
    private String unitName;
}
