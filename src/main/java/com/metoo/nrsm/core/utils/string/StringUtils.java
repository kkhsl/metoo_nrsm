package com.metoo.nrsm.core.utils.string;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-24 11:16
 */
public class StringUtils {

    public static boolean isNonEmptyAndTrimmed(String str) {
        return str != null && !str.trim().isEmpty();
    }

}
