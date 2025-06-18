package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-27 14:20
 */
@ApiModel("")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dns extends IdEntity {

    private String rtype;
    private String qname;
    private String address;

    private String primaryDomain;

}
