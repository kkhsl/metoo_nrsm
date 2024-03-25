package com.metoo.nrsm.core.utils.date;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
//
//        Date date = calendar.getTime();
//
//        System.out.println("加8小时后的时间是：" + calendar.getTime());
//        System.out.println("加8小时后的时间是：" + sdf.format(date));
//
        return null;

    }

    @Test
    public void test(){
        addZone("2024/01/15 02:39:27");
    }
}
