package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName metoo_dns_filter
 */
@Data
public class DnsFilter implements Serializable {


    @ApiModelProperty("")
    private Integer id;

    @ApiModelProperty("")
    private Date addTime;

    @ApiModelProperty("")
    private Date updateTime;

    private String domainName;

    private Integer status;

}
