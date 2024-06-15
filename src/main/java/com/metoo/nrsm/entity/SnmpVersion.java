package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-26 16:22
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SnmpVersion extends IdEntity {

    private String name;
    private Integer sequence;
}
