package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApiModel("终端单位表")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TerminalUnit extends IdEntity {

    @ApiModelProperty("单位名称")
    private String name;
    private List<TerminalUnitSubnet> terminaV4lList = new ArrayList<>();
    private List<TerminalUnitSubnetV6> terminaV6lList = new ArrayList<>();

    @ApiModelProperty("终端列表")
    private List<Terminal> terminalList = new ArrayList<>();

    private BigDecimal percentage;
}
