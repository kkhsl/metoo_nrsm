package com.metoo.nrsm.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemUsageVO {

    private double cpu_usage;
    private double mem_usage;
}
