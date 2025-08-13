package com.metoo.nrsm.core.utils.statis;

import cn.hutool.core.util.NumberUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数字处理工具
 * @author zzy
 * @version 1.0
 * @date 2024/11/23 10:18
 */
@Slf4j
@UtilityClass
public class NumUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_DASH = DateTimeFormatter.ofPattern("yyyyMMdd");
    public Double doubleAdd(Double v1,Double v2){
        return NumberUtil.add(v1==null?0d:v1,v2==null?0d:v2);
    }

    public Double doubleRadio(Double v1In,Double v1Out,Double v2In,Double v2Out){
        double v1= NumberUtil.add(v1In==null?0d:v1In,v1Out==null?0d:v1Out);
        double v2= NumberUtil.add(v2In==null?0d:v2In,v2Out==null?0d:v2Out);
        if(v1+v2==0){
            return 0D;
        }
        return NumberUtil.round(NumberUtil.div(v1,NumberUtil.add(v1,v2)),0).doubleValue();
    }

    public Double divUtil(Double v1,Double v2){
        if(v2==0){
            return 0D;
        }
        return NumberUtil.div(v1, v2, 2, RoundingMode.HALF_UP);
    }
    public Double divUtil(float v1,float v2){
        if(v2==0){
            return 0D;
        }
        return NumberUtil.div(v1, v2, 2, RoundingMode.HALF_UP);
    }

    public double divUtil(double v1,float v2){
        if(v2==0){
            return 0D;
        }
        return NumberUtil.div(v1, v2, 2, RoundingMode.HALF_UP);
    }
    /**
     * 根据输入的年月字符串，返回该月每一天的日期字符串数组
     *
     * @param yearMonthStr 输入的年月字符串，格式为'yyyy-MM'
     * @return 包含该月每一天日期（格式为"yyyyMMdd"）的字符串数组
     */
    public static List<Integer> getDaysOfMonthArray(String yearMonthStr) {
        // 定义日期格式
        DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 解析输入的年月字符串为YearMonth对象
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, yearMonthFormatter);
        // 获取该月的第一天和最后一天
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        // 计算该月的天数
        int daysInMonth = lastDay.getDayOfMonth();

        // 创建一个数组来存储日期字符串
        String[] daysArray = new String[daysInMonth];

        // 遍历该月的每一天，并格式化日期为字符串
        for (int day = 0; day < daysInMonth; day++) {
            LocalDate currentDay = firstDay.plusDays(day);
            daysArray[day] = currentDay.format(dayFormatter);
        }
        return  Arrays.stream(daysArray).map(Integer::parseInt).collect(Collectors.toList());
    }

    /**
     * 根据输入的日期字符串，返回该天每一个小时的字符串数组
     *
     * @param dateStr 输入的日期字符串，格式为'yyyy-MM-dd'
     * @return 包含该天每一个小时（格式为"yyyyMMddHH"）的字符串数组
     */
    public static List<Integer> getHoursOfDayArray(String dateStr) {
        // 定义日期格式
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 解析输入的日期字符串为LocalDate对象
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);

        // 创建一个数组来存储小时字符串
        String[] hoursArray = new String[24];

        // 遍历该天的每一个小时，并格式化日期时间为字符串
        for (int hour = 0; hour < 24; hour++) {
            hoursArray[hour] = date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%02d", hour);
        }
        return  Arrays.stream(hoursArray).map(Integer::parseInt).collect(Collectors.toList());
    }

    public static List<Integer> getAllMonth(String year){
        List<Integer> months = new ArrayList<>();
        for (Month month : Month.values()) {
            months.add(Integer.parseInt(String.format("%d%02d", Integer.valueOf(year), month.getValue())));
        }
        return months;
    }
    public static List<String> getAllWeek(){
        List<String> weeks = new ArrayList<>();
        weeks.add("周一");
        weeks.add("周二");
        weeks.add("周三");
        weeks.add("周一");
        weeks.add("周一");
        weeks.add("周一");

        return weeks;
    }


    /**
     * 获取指定日期所在周的周一和周日日期
     * @param dateStr 输入的日期字符串（格式：yyyy-MM-dd）
     * @return 字符串数组，[0]=周一日期，[1]=周日日期
     */
    public static String[] getWeekStartEndDates(String dateStr) {
        LocalDate inputDate = LocalDate.parse(dateStr, DATE_FORMATTER);

        // 计算周一和周日
        LocalDate monday = inputDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate sunday = inputDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        return new String[]{
                monday.format(DATE_FORMATTER_DASH),
                sunday.format(DATE_FORMATTER_DASH)
        };
    }

    public static Map<String, String> getWeekDatesWithDays(String dateStr) {
        LocalDate inputDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        LocalDate monday = inputDate.with(DayOfWeek.MONDAY); // 定位本周一

        Map<String, String> dateMap = new LinkedHashMap<>();
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        // 生成周一到周日的日期及星期映射
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = monday.plusDays(i);
            String dateKey = currentDate.format(DATE_FORMATTER_DASH);
            dateMap.put(dateKey, weekDays[i]);
        }
        return dateMap;
    }

    public static void main(String[] args) {
        Map<String, String> weekMap = getWeekDatesWithDays("2025-08-12");
        System.out.println("日期\t\t星期");
        weekMap.forEach((date, day) -> System.out.println(date + "\t" + day));
//        // 输入的日期字符串
//        String input = "2024-11-01";
//
//        // 调用方法来获取小时字符串数组，并输出结果
//        List<Integer> hoursArray = getHoursOfDayArray(input);
//        System.out.println("小时字符串数组: ");
//        for (Integer hourStr : hoursArray) {
//            System.out.println(hourStr);
//        }
//
//        // 输入的年月字符串
//        String inputStr = "2024-11";
//
//        // 调用方法来获取日期字符串数组，并输出结果
//        List<Integer> daysArray = getDaysOfMonthArray(inputStr);
//        System.out.println("日期字符串数组: ");
//        for (Integer dayStr : daysArray) {
//            System.out.println(dayStr);
//        }
        String[] dates=getWeekStartEndDates("2025-08-12");
        for (String dayStr : dates) {
            System.out.println(dayStr);
        }
    }
}
