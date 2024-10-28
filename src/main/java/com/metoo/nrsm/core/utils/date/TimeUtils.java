package com.metoo.nrsm.core.utils.date;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-17 17:11
 */
public class TimeUtils {

//    private static final String DATE_PATTERN = "^\\d{4}/\\d{2}/\\d{2}$";
    private static final String DATE_PATTERN = "^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}$";


    public static boolean validateDate(String date) {
        Pattern pattern = Pattern.compile(DATE_PATTERN);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }

    public static void main(String[] args) {
        String date2 = "2024/01/15 02:39:27";

        System.out.println(validateDate(date2)); // false
    }


    @Test
    public void addZoneTest(){
        addZone("2024/01/15 02:39:27");
    }

    // 增加8个时区
    public static String addZone(String time){
        boolean flag = validateDate(time);
        if(flag){
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        String currentTime = "2024/01/15 02:44:01";
            try {
                Date date1 = sdf2.parse(time);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(date1);
                calendar2.add(Calendar.HOUR_OF_DAY, 8);

                Date date3 = calendar2.getTime();
//                System.out.println("加8小时后的时间是：" + sdf2.format(date3));
                return sdf2.format(date3);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//        LocalDateTime localDateTime = LocalDateTime.now();
//
//        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
//
//        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
//
//        System.out.println("日期和时间（8时区）：" + zonedDateTime);
//
//        final Date from = Date.from(zonedDateTime.toInstant()); //ZoneDateTime 转换成Date
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:dd:ss");
//
//        System.out.println("日期和时间（8时区）：" + sdf.format(from));
//
//        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));

//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, 8);
//        Date date = calendar.getTime();
//        System.out.println("加8小时后的时间是：" + calendar.getTime());
//        System.out.println("加8小时后的时间是：" + sdf.format(date));
//
        return null;

    }

    /**
     * 获取当前时间
     *  传统方式：Date/Calendar
     *  java8引入的新的日期时间API：LocalDateTime/Instant/ZoneDateTime等
     */
    @Test
    public void date(){
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();

        // 打印当前时间
        System.out.println(calendar.getTime()); // 输出：Tue May 28 02:59:59 CST 2024
    }

    @Test
    public void calendar(){
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();

        // 打印当前时间
        System.out.println(calendar.getTime()); // 输出：Tue May 28 02:59:59 CST 2024
    }

    // 2024-05-28T02:59:59 这种格式是 ISO 8601 标准日期和时间的表示形式
    @Test
    public void localDateTime(){

        // 创建一个 LocalDateTime 实例
        LocalDateTime dateTime = LocalDateTime.of(2024, 5, 28, 2, 59, 59, 0);

        // 输出时间
        System.out.println(dateTime); // 输出格式：2024-05-21T02:59:59

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        System.out.println(now); // 输出格式：2024-05-28T10:21:56.751


        // 自定义格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dateTime.format(formatter)); // 2024-05-28 02:59:59

        System.out.println(new Date());
    }

    @Test
    public void instant(){
        // 获取当前时间的时间戳
        Instant now = Instant.now();

        // 打印当前时间戳
        System.out.println(now); // 输出：2024-05-28T02:23:27.090Z
    }

    @Test
    public void zonedDateTime(){
        // 获取当前时间及其时区
        ZonedDateTime now = ZonedDateTime.now();

        // 打印当前时间及其时区
        System.out.println(now); // 输出：2024-05-28T10:24:18.550+08:00[Asia/Shanghai]


        // 格式化输出当前时间及其时区
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        String formattedNow = now.format(formatter);
        System.out.println(formattedNow); // 输出：2024-05-28 10:24:48 +0800

        // 解析字符串为 ZonedDateTime 对象
        String dateTimeString = "2024-05-28 02:59:59 +0800";
        ZonedDateTime parsedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
        System.out.println("Parsed DateTime: " + parsedDateTime); // 示例输出：2024-05-28T02:59:59+08:00

        // 格式化 ZonedDateTime 对象为字符串
        String formattedDateTime = parsedDateTime.format(formatter);
        System.out.println("Formatted DateTime: " + formattedDateTime); // 示例输出：2024-05-28 02:59:59 +0800
    }

}
