package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@ApiModel("用户DTO")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GatewayDTO extends PageDto<Gateway> {

    private Date addTime;
    private String name;
    private String ip;
    private String vendor;
    private String loginType;
    private String loginPort;
    private String loginName;
    private String loginPassword;
}