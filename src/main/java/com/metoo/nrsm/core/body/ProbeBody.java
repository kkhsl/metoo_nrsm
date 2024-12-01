package com.metoo.nrsm.core.body;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProbeBody {

    @ApiModelProperty("任务ID 流水号")
    private String taskuuid;
    @ApiModelProperty("返回结果 每个ip+port的扫描结果保存在json格式字符串中,最后所有的结果保存在一个列表中")
    private String result;
}
