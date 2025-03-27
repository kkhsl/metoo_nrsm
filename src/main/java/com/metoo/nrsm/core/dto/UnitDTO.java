package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Unit;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@ApiModel("用户DTO")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UnitDTO extends PageDto<Unit> {

    private Long id;
    private Date addTime;
    private String name;
    private String unitName;
    private String department;
    private String area;
    private String city;
    private String vlanNum;
    private Long gatewayId;
    private String pattern;
    private Integer deleteStatus;

}