package com.metoo.nrsm.core.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("测绘日志输出父模块vo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyingLogVo {

    @ApiModelProperty("Id")
    private Integer id;

    @ApiModelProperty("模块名称")
    private String name;

    @ApiModelProperty("开始时间")
    private String beginTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("采集状态 默认0：未采集 1：正在采集 2：采集完成 3：采集失败")
    private Integer status;

    @ApiModelProperty("备注")
    private String desc;

    @ApiModelProperty("采集成功失败：默认 1：成功 0：失败")
    private Integer info;

    private Integer type;

}
