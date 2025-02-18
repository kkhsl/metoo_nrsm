package com.metoo.nrsm.core.config.ssh.utils;

import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.entity.Unit;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    private static String[] parsePatterns = new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    public DateUtils() {
    }

    public static String getDate() {
        return getDate("yyyy-MM-dd");
    }

    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    public static String formatDate(Date date, Object... pattern) {
        String formatDate = null;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }

        return formatDate;
    }

    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }


    public static void main(String[] args) {

        Date date = new Date();
        String time = DateUtils.getDateTimeWithZeroSeconds(date);
        long currentTime =
                DateUtils.convertDateStringToTimestamp(time, "yyyy-MM-dd HH:mm:ss");
        String currentTimestamp = String.valueOf(currentTime);
        System.out.println(time);
        System.out.println(currentTimestamp);
    }

    /**
     * 将字符串日期转换为时间戳
     *
     * @param dateString 日期字符串
     * @param format 日期格式
     * @return 时间戳（毫秒）
     * @throws ParseException 如果日期字符串无法解析
     */
    public static long convertDateStringToTimestamp(String dateString, String format)  {
        // 创建 SimpleDateFormat 对象
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        // 解析字符串为 Date 对象
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获取时间戳（以毫秒为单位）
        return date.getTime();
    }


    public static String getCurrentTimeMillis(Date date){
        // 创建 Date 对象，表示当前时间
        Date now = new Date();

        // 获取当前时间的时间戳（以毫秒为单位）
        long timestamp = now.getTime();
        String timestampString = String.valueOf(timestamp);
        return timestampString;
    }

    // 定义一个方法来获取格式化日期时间，秒钟部分全部为0
    public static String getDateTimeWithZeroSeconds(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // 获取各个时间组件
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 月份从0开始，所以需要加1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String formattedDateTime = String.format("%04d-%02d-%02d %02d:%02d",
                year, month, day, hour, minute);

        return formattedDateTime + ":00";
//        return "2024-08-12 22:05:00";
    }

    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    public static String getDay() {
        return formatDate(new Date(), "dd");
    }

    public static String getWeek() {
        return formatDate(new Date(), "E");
    }

    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        } else {
            try {
                return parseDate(str.toString(), parsePatterns);
            } catch (ParseException var2) {
                return null;
            }
        }
    }

    public static long pastDays(Date date) {
        long t = System.currentTimeMillis() - date.getTime();
        return t / 86400000L;
    }

    public static long pastHour(Date date) {
        long t = System.currentTimeMillis() - date.getTime();
        return t / 3600000L;
    }

    public static long pastMinutes(Date date) {
        long t = System.currentTimeMillis() - date.getTime();
        return t / 60000L;
    }

    public static String formatDateTime(long timeMillis) {
        long day = timeMillis / 86400000L;
        long hour = timeMillis / 3600000L - day * 24L;
        long min = timeMillis / 60000L - day * 24L * 60L - hour * 60L;
        long s = timeMillis / 1000L - day * 24L * 60L * 60L - hour * 60L * 60L - min * 60L;
        long sss = timeMillis - day * 24L * 60L * 60L * 1000L - hour * 60L * 60L * 1000L - min * 60L * 1000L - s * 1000L;
        return (day > 0L ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
    }

    public static double getDistanceOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (double)((afterTime - beforeTime) / 86400000L);
    }

    public static int getDistanceOfTwoDateHour(Long beforeTime, Long afterTime) {
        return (int)((afterTime - beforeTime) / 3600L);
    }

    public static Long getHourTimestamp(Long datetime) {
        Long tmp1 = datetime / 3600L;
        Long tmp2 = tmp1 * 3600L;
        return tmp2;
    }

    public static Long getDayTimestamp(Long datetime) {
        Long tmp1 = (datetime + 28800L) / 86400L;
        Long tmp2 = Long.valueOf(tmp1 * 86400L) - 28800L;
        return tmp2;
    }

    public static int getDistanceOfTwoDateDay(Long beforeTime, Long afterTime) {
        return (int)((afterTime - beforeTime) / 86400L);
    }

    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() != startTime.getTime() && nowTime.getTime() != endTime.getTime()) {
            Calendar date = Calendar.getInstance();
            date.setTime(nowTime);
            Calendar begin = Calendar.getInstance();
            begin.setTime(startTime);
            Calendar end = Calendar.getInstance();
            end.setTime(endTime);
            return date.after(begin) && date.before(end);
        } else {
            return true;
        }
    }

    public static boolean beforeEndDate(Date nowTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return date.before(end);
    }
}
