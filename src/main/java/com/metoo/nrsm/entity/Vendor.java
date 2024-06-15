package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Vendor extends IdEntity {

    private String name;

    private Integer index;

    private String uuid;

    private Integer number;

    private String nameEn;

}
