package com.metoo.nrsm.core.utils.date;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeekendChecker {

    // 方法：判断给定日期是否为周六或周日
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }


    public static boolean isWeekend(LocalDateTime date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static void main(String[] args) {
        LocalDate date1 = LocalDate.of(2024, 8, 24); // 这是一个周六
        LocalDate date2 = LocalDate.of(2024, 8, 26); // 这是一个周一

        System.out.println(date1 + " is weekend? " + isWeekend(date1)); // true
        System.out.println(date2 + " is weekend? " + isWeekend(date2)); // false
    }




}
