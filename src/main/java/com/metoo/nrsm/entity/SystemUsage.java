package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemUsage extends IdEntity {

    private double cpu_usage;
    private double mem_usage;
}
