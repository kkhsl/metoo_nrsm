package com.metoo.nrsm.entity;


import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@ApiModel("拓扑管理")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Topology extends IdEntity {

    private String name;
    private String suffix;
    private Date updateTime;
    private Boolean isDefault;
    private String description;
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private Object content;
    private String baseUrl;

    private boolean nf;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String location;
}
