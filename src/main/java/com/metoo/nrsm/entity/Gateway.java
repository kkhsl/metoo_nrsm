package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gateway extends IdEntity {

    private String name;
    private String ip;
//    private String vendor;

    @ApiModelProperty("设备品牌Id")
    private Long vendorId;
    private String vendorName;
    private String vendorAlias;

    private String loginType;
    private String loginPort;
    private String loginName;
    private String loginPassword;

    private String uuid;
}
