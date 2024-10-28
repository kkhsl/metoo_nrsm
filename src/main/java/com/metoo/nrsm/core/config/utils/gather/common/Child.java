package com.metoo.nrsm.core.config.utils.gather.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-25 10:54
 */
@Data
@Accessors(chain = true)
public class Child extends Parent {

    private String test;

}