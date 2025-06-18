package com.metoo.nrsm.core.utils.date;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class TimeRangeChecker {


    // 方法：判断当前时间是否在21:30到次日7:30之间
    public static boolean isWithinNightRange(LocalTime time) {
        LocalTime startTime = LocalTime.of(21, 30); // 21:30
        LocalTime endTime = LocalTime.of(7, 30); // 次日7:30

        // 判断时间是否在21:30到午夜12:00之间，或者在午夜12:00到7:30之间
        return (time.isAfter(startTime) || time.equals(startTime)) || (time.isBefore(endTime) || time.equals(endTime));
    }


    public static boolean isWithinTimeRange(LocalDateTime dateTime) {
        LocalTime currentTime = dateTime.toLocalTime();
//        LocalTime startTime = LocalTime.of(21, 30); // 21:30
//        LocalTime endTime = LocalTime.of(7, 30);   // 7:30

        LocalTime startTime = LocalTime.of(17, 30); // 21:30
        LocalTime endTime = LocalTime.of(8, 20);   // 7:30

        // 时间范围跨越午夜的处理
        if (startTime.isBefore(endTime)) {
            // 如果时间范围没有跨越午夜
            return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
        } else {
            // 如果时间范围跨越午夜
            return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
        }
    }

//    public static double getRandomNumer(LocalDateTime dateTime){
//        double number = 0;
//        LocalTime currentTime = dateTime.toLocalTime();
//        if(isWithinTimeRange(dateTime)){
//            boolean flag = false;
//            flag = isWithinTimeRange(17, 30, 17, 35, currentTime);
//            if(flag){
//               number = generateRandomDecimal(1.2, 1.5, 2);
//            }else{
//
//            }
//        }
//    }

    public static boolean isWithinTimeRange(int start_hour, int start_minute, int end_hour, int end_minute, LocalTime currentTime) {

        LocalTime startTime = LocalTime.of(start_hour, start_minute); // 21:30
        LocalTime endTime = LocalTime.of(end_hour, end_minute);   // 7:30

        // 时间范围跨越午夜的处理
        if (startTime.isBefore(endTime)) {
            // 如果时间范围没有跨越午夜
            return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
        } else {
            // 如果时间范围跨越午夜
            return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
        }
    }


    private static final Random random = new Random();

    public static double generateRandomDecimal(double min, double max, int decimalPlaces) {
        double range = max - min;
        double randomValue = min + range * random.nextDouble();
        double scalingFactor = Math.pow(10, decimalPlaces);
        return Math.round(randomValue * scalingFactor) / scalingFactor;
    }

    public static void main(String[] args) {
        LocalTime time1 = LocalTime.of(22, 0);  // 22:00
        LocalTime time2 = LocalTime.of(6, 0);   // 06:00
        LocalTime time3 = LocalTime.of(8, 0);   // 08:00
        LocalTime time4 = LocalTime.of(21, 30); // 21:30

        System.out.println(time1 + " is within range? " + isWithinNightRange(time1)); // true
        System.out.println(time2 + " is within range? " + isWithinNightRange(time2)); // true
        System.out.println(time3 + " is within range? " + isWithinNightRange(time3)); // false
        System.out.println(time4 + " is within range? " + isWithinNightRange(time4)); // true
    }
}
