package com.metoo.nrsm.core.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * dns记录数据vo
 * @author zzy
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DnsRecordVo {


    @ApiModelProperty("记录时间")
    private String recordTime;

    @ApiModelProperty("域名")
    private String domainData;

    @ApiModelProperty("访问次数")
    private Integer num;

}
