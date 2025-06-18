package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Vlan;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class VlanDTO extends PageDto<Vlan> {

    private String name;
    private int number;
    private String description;
    private Long domainId;
    private Date editDate;
    @ApiModelProperty("所属子网/网段")
    private String subnet;
    private Long groupId;
}
