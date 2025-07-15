package com.metoo.nrsm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSysInfoDTO {

    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;// 添加时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("系统制造商")
    private String manufacturer;

    @ApiModelProperty("系统型号")
    private String model;

    @ApiModelProperty("操作系统")
    private String os;

    private List<String> cpu = new ArrayList<>();

    private List<String> macAddresses = new ArrayList<>(); // 改为驼峰命名
}
