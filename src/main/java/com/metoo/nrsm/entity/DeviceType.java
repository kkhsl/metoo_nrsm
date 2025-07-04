package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@ApiModel("设备类型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceType extends IdEntity {

    private String name;
    private String nameEn;
    private Integer count;
    private Integer onlineCount;
    private Integer online;
    private Integer type;

    private Integer sequence;
    @ApiModelProperty("默认 0: 设备类型 1：终端类型")
    private Integer diff;

    private String uuid;// 可用作自定义图片名称

    private List<Vendor> vendors = new ArrayList<>();


    private List<NetworkElement> networkElementList = new ArrayList<>();
    private List<Terminal> terminalList = new ArrayList<>();

}
