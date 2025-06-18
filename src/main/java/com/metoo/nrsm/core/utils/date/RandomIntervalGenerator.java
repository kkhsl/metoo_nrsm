package com.metoo.nrsm.core.utils.date;

import java.time.LocalTime;
import java.util.Random;

public class RandomIntervalGenerator {

    private static final Random random = new Random();

    public static double generateRandomDecimal(double min, double max, int decimalPlaces) {
        double range = max - min;
        double randomValue = min + range * random.nextDouble();
        double scalingFactor = Math.pow(10, decimalPlaces);
        return Math.round(randomValue * scalingFactor) / scalingFactor;
    }

    public static double generateRandomNumbersForCurrentInterval(LocalTime currentTime) {
        // 定义时间区间和对应的范围
        String[][] timeIntervals = {
                {"08:05", "08:10"}, {"08:10", "08:15"}, {"08:15", "08:20"},
                {"17:30", "17:35"}, {"17:35", "17:40"}, {"17:40", "17:45"},
                {"17:45", "17:50"}, {"17:50", "17:55"}, {"17:55", "18:00"},
                {"18:00", "18:05"}, {"18:05", "18:10"}, {"18:10", "18:15"},
                {"18:15", "18:20"}, {"18:20", "18:25"}, {"18:25", "18:30"},
                {"18:30", "18:35"}, {"18:35", "18:40"}, {"18:40", "18:45"},
                {"18:45", "18:50"}, {"18:50", "18:55"}, {"18:55", "19:00"},
                {"19:00", "08:00"}
        };

        double[][] ranges = {
                {1.7, 2.3}, {1.4, 1.7}, {1.2, 1.4},
                {1.2, 1.5}, {1.2, 1.5}, {1.3, 1.6},
                {1.4, 1.7}, {1.7, 2.0}, {2.0, 3.0},
                {2.5, 4.0}, {4.0, 6.0}, {5.0, 7.0},
                {7.0, 9.0}, {7.0, 9.0}, {9.0, 10.0},
                {9.0, 10.0}, {9.0, 10.0}, {10.0, 12.0},
                {10.0, 12.0}, {12.0, 15.0}, {12.0, 15.0},
                {15.0, 20.0}
        };

        LocalTime startTime, endTime;

        // 查找当前时间对应的时间区间和范围
        for (int i = 0; i < timeIntervals.length; i++) {
            startTime = LocalTime.parse(timeIntervals[i][0]);
            endTime = LocalTime.parse(timeIntervals[i][1]);

            if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                double min = ranges[i][0];
                double max = ranges[i][1];
                double number = generateRandomDecimal(min, max, 1);
                System.out.println("当前时间: " + currentTime);
                System.out.println("时间区间: " + timeIntervals[i][0] + " - " + timeIntervals[i][1]);
                return number;
            }
        }
        System.out.println("当前时间不在指定的时间区间内。");
        return 20;
    }

    public static void main(String[] args) {
        // 获取当前时间
        LocalTime currentTime = LocalTime.now();
        generateRandomNumbersForCurrentInterval(currentTime);
    }
}
