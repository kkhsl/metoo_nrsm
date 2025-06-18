package com.metoo.nrsm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-17 17:02
 */
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class Nswitch {

    private Integer index;

    private String name;
}
