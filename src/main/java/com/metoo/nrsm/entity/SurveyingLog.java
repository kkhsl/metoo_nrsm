package com.metoo.nrsm.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyingLog {

    @ApiModelProperty("Id")
    private Integer id;

    private String addTime;

    private String name;

    private String beginTime;

    private String endTime;

    @ApiModelProperty("采集状态 默认0：未采集 1：正在采集 2：采集完成 3：采集失败")
    private Integer status;

    private String desc;

    @ApiModelProperty("父日志id")
    private Integer parentId;

    @ApiModelProperty("采集成功失败：默认 1：成功 0：失败")
    private Integer info;

    private Integer type;

    public SurveyingLog addTime(String addTime) {
        this.addTime = addTime;
        return this;
    }

    public SurveyingLog name(String name) {
        this.name = name;
        return this;
    }

    public SurveyingLog beginTime(String beginTime) {
        this.beginTime = beginTime;
        return this;
    }

    public SurveyingLog endTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public SurveyingLog status(Integer status) {
        this.status = status;
        return this;
    }

    public SurveyingLog desc(String desc) {
        this.desc = desc;
        return this;
    }

    public SurveyingLog info(Integer info) {
        this.info = info;
        return this;
    }

    public SurveyingLog parentId(Integer parentId) {
        this.parentId = parentId;
        return this;
    }

    public SurveyingLog type(Integer type) {
        this.type = type;
        return this;
    }

}
