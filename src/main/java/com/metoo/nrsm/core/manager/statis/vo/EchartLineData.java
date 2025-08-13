package com.metoo.nrsm.core.manager.statis.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * echart 多维线表、柱状数据
 * @author zzy
 * @version 1.0
 * @date 2024/11/23 9:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EchartLineData {
    /**
     * x轴titlte
     */
    @ApiModelProperty("x轴titlte")
    private List<String> title;
    /**
     * y轴value
     */
    @ApiModelProperty("y轴value")
    private List<EchartData> value;


}
