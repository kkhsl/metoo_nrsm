package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-21 10:42
 */
@ApiModel("评分-月比例")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FluxDailyRate extends IdEntity {

    private BigDecimal rate;

    private boolean flag;
}
