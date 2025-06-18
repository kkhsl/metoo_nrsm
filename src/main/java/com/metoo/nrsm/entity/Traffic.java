package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("流量")
@Data
public class Traffic extends IdEntity {

    @ApiModelProperty("仅需上传，该类型使用String")
    private String vfourFlow;

    private String vsixFlow;

    private String unitName;

}
