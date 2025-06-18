package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalUnitSubnetV6 extends IdEntity {

    @ApiModelProperty("单位名称")
    private String ip;
    private String mask;
    private Long terminalUnitId;

}
