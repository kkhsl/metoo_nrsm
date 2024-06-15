package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-30 15:12
 */
@ApiModel("评分-设备比例权重")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeWeight {

    private BigDecimal ne;

    private BigDecimal terminal;

    private BigDecimal flux;

    /*流量达标*/
    private BigDecimal reach;
}
