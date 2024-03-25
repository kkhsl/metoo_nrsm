package com.metoo.nrsm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-29 15:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Internet {

    private String v4status;
    private String v4int;
    private String v6status;
    private String v6int;

}
