package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@ApiModel("")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SubnetIpv6 extends IdEntity {

    private String ip;
    private Integer mask;
    private Long parentId;
    private String parentIp;
    private List<SubnetIpv6> subnetList;
    private String description;

}
