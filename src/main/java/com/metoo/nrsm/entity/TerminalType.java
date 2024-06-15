package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalType extends IdEntity {

    @ApiModelProperty("终端类型名称")
    private String name;
    @ApiModelProperty("终端类型")
    private Integer type;
    @ApiModelProperty("排序")
    private Integer sequence;
}
