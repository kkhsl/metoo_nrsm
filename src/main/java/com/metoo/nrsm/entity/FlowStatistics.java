package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 16:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowStatistics extends IdEntity {

    private BigDecimal ipv4Sum;
    private BigDecimal ipv6Sum;
    private BigDecimal ipv4;
    private BigDecimal ipv6;


    private BigDecimal ipv6Rate;
}
