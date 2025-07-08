package com.metoo.nrsm.core.config.aop.idempotent;

import java.lang.annotation.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 16:05
 * <p>
 * 幂等性校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotRepeat {
}
