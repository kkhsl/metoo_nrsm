package com.metoo.nrsm.core.manager.statis.vo;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 流量占比数据
 * @author zzy
 * @version 1.0
 * @date 2024/11/23 9:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowRadioDataExport {
    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String title;
    /**
     * 部门编码
     */
    @ApiModelProperty("部门编码")
    private String id;
    /**
     * 部门名称
     */
    @ApiModelProperty( "部门名称")
    private String name;

    /**
     * ipv4流量
     */
    @ApiModelProperty("ipv4流量")
    private Double ipv4;
    /**
     * ipv6流量
     */
    @ApiModelProperty( "ipv6流量")
    private Double ipv6;

    /**
     * ipv6流量
     */
    @ApiModelProperty( "总流量")
    private Double total;
    /**
     * ipv6流量占比
     * */
    @ApiModelProperty( "ipv6流量占比")
    private Double ipv6Radio;


    public Double getIpv4() {
        if(ObjectUtil.isNotEmpty(ipv4)){
            BigDecimal roundTemp= NumberUtil.div(new BigDecimal(ipv4),1000,3);
            return roundTemp.doubleValue();
        }
        return ipv4;
    }
    public Double getTotal() {
        if(ObjectUtil.isNotEmpty(total)){
            BigDecimal roundTemp= NumberUtil.div(new BigDecimal(total),1000,3);
            return roundTemp.doubleValue();
        }
        return ipv4;
    }
    public Double getIpv6() {
        if(ObjectUtil.isNotEmpty(ipv6)){
            BigDecimal roundTemp= NumberUtil.div(new BigDecimal(ipv6),1000,3);
            return roundTemp.doubleValue();
        }
        return ipv6;
    }
}
