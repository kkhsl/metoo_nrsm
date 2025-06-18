package com.metoo.nrsm.core.config.utils.gather.factory.gather;

import com.metoo.nrsm.core.utils.date.TimeRangeChecker;
import com.metoo.nrsm.core.utils.date.WeekendChecker;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlowUtils {

    // 重载方法：没有参数时使用当前日期时间
    public static boolean isWithinTimeRange() {
        return checkCurrentTime(LocalDateTime.now());
    }

    // 周末和（工作日21:30-7:30）按实际数据
    public static boolean checkCurrentTime(LocalDateTime now){

//        LocalDateTime now = LocalDateTime.now(); // 当前日期时间

        boolean flag1 = WeekendChecker.isWeekend(now);

        boolean flag2 = TimeRangeChecker.isWithinTimeRange(now);

        if(flag1 || flag2){
            return true;
        }
        return false;
    }

    @Test
    public void calulateFlowTest(){
        double ipv4Inbound1 = 0.0000000000000000000005; // 示例输入值
        double result = calculateFlow(ipv4Inbound1);
        System.out.println("Processed flow value: " + result);
    }


    /**
     * 计算并返回处理后的流量值，保留两位小数
     * @param ipv4Inbound1 输入值
     * @return 处理后的流量值
     */
    public static double calculateFlow(double ipv4Inbound1) {
        // 使用 BigDecimal 处理浮点数的精确计算
        BigDecimal inbound = BigDecimal.valueOf(ipv4Inbound1);
        BigDecimal threshold = BigDecimal.valueOf(0.01);

        // 判断条件
        boolean isInboundLow = inbound.compareTo(threshold) < 0 && inbound.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal vfourFlow;
        if (isInboundLow) {
            // 如果条件满足，设置 vfourFlow 为 0.01
            vfourFlow = threshold;
        } else {
            // 否则，确保 ipv4Inbound1 在需要的范围内
            if (inbound.compareTo(threshold) < 0 && inbound.compareTo(BigDecimal.ZERO) > 0) {
                inbound = threshold;
            }
            // 计算 vfourFlow
            vfourFlow = inbound.add(inbound); // 相当于 inbound * 2
        }

        // 保留两位小数
        return vfourFlow.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static void main(String[] args) {

        LocalDateTime time1 = LocalDateTime.of(2024,8,27,22, 0);  // 22:00
        LocalDateTime time2 = LocalDateTime.of(2024,8,27,6, 0);   // 06:00
        LocalDateTime time3 = LocalDateTime.of(2024,8,27,8, 0);   // 08:00
        LocalDateTime time4 = LocalDateTime.of(2024,8,31,21, 30); // 21:30

        System.out.println(time1 + " is within range? " + checkCurrentTime(time1)); // true
        System.out.println(time2 + " is within range? " + checkCurrentTime(time2)); // true
        System.out.println(time3 + " is within range? " + checkCurrentTime(time3)); // false
        System.out.println(time4 + " is within range? " + checkCurrentTime(time4)); // true
    }
}
