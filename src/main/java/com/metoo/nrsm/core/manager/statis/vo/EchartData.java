package com.metoo.nrsm.core.manager.statis.vo;

import cn.hutool.core.collection.CollUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * echart数据
 * @author zzy
 * @version 1.0
 * @date 2024/11/23 9:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EchartData {
    /**
     * 名称
     */
    @ApiModelProperty( "名称")
    private String name;
    /**
     * 数据值
     */
    @ApiModelProperty( "数据值")
    private List<Double> data;

    public List<Double> getData() {
        if(CollUtil.isNotEmpty(data)){
            List<Double> result=new ArrayList<>();
            for (Double o : data) {
                result.add(new BigDecimal(o).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            return result;
        }
        return data;
    }
}
