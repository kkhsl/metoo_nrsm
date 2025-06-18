package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2023-11-07 16:06
 */
@ApiModel("日志管理")
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class OperationLog extends IdEntity {

    @ApiModelProperty("操作用户账号")
    private String account;
    @ApiModelProperty("操作用户姓名")
    private String name;
    @ApiModelProperty("操作动作 例如：访问某个页面、CRUD等")
    private String action;
    @ApiModelProperty("行为描述")
    private String desc;
    @ApiModelProperty("日志类型 默认0：操作日志 1：访问日志 2：登录日志")
    private Integer type;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("部门代码")
    private String DM;
    @ApiModelProperty("部门名称")
    @JsonProperty("MC")
    private String MC;

}
