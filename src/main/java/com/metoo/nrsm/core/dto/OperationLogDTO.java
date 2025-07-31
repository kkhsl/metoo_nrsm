package com.metoo.nrsm.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.OperationLog;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2023-11-07 16:16
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogDTO extends PageDto<OperationLog> {

    @ApiModelProperty("操作用户账号")
    private String account;
    @ApiModelProperty("操作用户姓名")
    private String name;
    @ApiModelProperty("操作动作 例如：访问某个页面、CRUD等")
    private String action;
    @ApiModelProperty("行为描述")
    private String desc;
    @ApiModelProperty("日志类型 默认0：操作日志 1：访问日志")
    private Integer type;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("部门代码")
    private String DM;
    @ApiModelProperty("部门名称")
    private String MC;

    @ApiModelProperty("开始时间（格式：yyyy-MM-dd HH:mm:ss）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty("结束时间（格式：yyyy-MM-dd HH:mm:ss）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private String other;
}
