package com.metoo.nrsm.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.nspm.Terminal;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Set;

@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class TerminalDTO extends PageDto<Terminal> {

    private String filter;

//    @NotNull("终端名称不能为空")
    private String name;

    @ApiModelProperty("设备Ip")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("Mac地址")
    private String mac;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("端口状态")
    private Integer interfaceStatus;

    @ApiModelProperty("接口ip")
    private String ip;

    @ApiModelProperty("是否在线")
    private Boolean online;

    @ApiModelProperty("部门Id")
    private Long departmentId;

    private Set<Long> departmentIds;

    // 终端类型改用设备类型
    @ApiModelProperty("设备类型")
    private Long deviceTypeId;

    @ApiModelProperty("采购时间")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date purchase_time;

    @ApiModelProperty("过保时间")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date warranty_time;

    @ApiModelProperty("序列号")
    private String serial_number;

    @ApiModelProperty("资产编号")
    private String asset_number;

    @ApiModelProperty("来源 0：采集 1：手动录入")
    private Integer from;

    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date start_purchase_time;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date end_purchase_time;
    @ApiModelProperty("过保时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date start_warranty_time;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date end_warranty_time;


    @ApiModelProperty("项目Id")
    private Long projectId;
    @ApiModelProperty("项目名")
    private String projectName;
    @ApiModelProperty("厂商ID")
    private Long vendorId;
    @ApiModelProperty("厂商名称")
    private String vendorName;
    @ApiModelProperty("型号")
    private String model;


}
