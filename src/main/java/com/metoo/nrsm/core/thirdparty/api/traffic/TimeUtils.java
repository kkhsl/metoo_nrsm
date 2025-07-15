package com.metoo.nrsm.core.thirdparty.api.traffic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前时间的 LocalDateTime 实例
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    /**
     * 将传入时间的秒和毫秒清零
     */
    public static LocalDateTime clearSecondAndNano(LocalDateTime time) {
        return time.withSecond(0).withNano(0);
    }

    /**
     * 获取传入时间的前 5 分钟，秒和毫秒清零
     */
    public static LocalDateTime getFiveMinutesBefore(LocalDateTime baseTime) {
        return baseTime.minusMinutes(5).withSecond(0).withNano(0);
    }

    /**
     * 格式化 LocalDateTime 为字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String format(LocalDateTime time) {
        return time.format(FORMATTER);
    }
}
