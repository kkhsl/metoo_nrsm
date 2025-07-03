package com.metoo.nrsm.entity;

import lombok.Data;

import java.util.Date;

@Data
public class FtpConfig {
    private Integer id;
    private String ftpHost;
    private String ftpPort;
    private String userName;
    private String password;
    private String filePath;
    private Boolean deleteStatus;
    private Long createUser;
    private Date createTime;
    private Long updateUser;
    private Date updateTime;

}