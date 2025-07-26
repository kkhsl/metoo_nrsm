package com.metoo.nrsm.core.utils.date;

import org.junit.Test;
import org.springframework.stereotype.Component;
import org.threeten.bp.Duration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * String.format("%tY", new Date())    //2011
 * String.format("%tm", new Date())   //03
 * String.format("%tF", new Date())    //2011-03-04
 * String.format("%tR", new Date())   //15:49
 * String.format("%tT", new Date())   //15:49:34
 * String.format("%tc", new Date())   //星期五 三月 04 15:49:34 CST 2011
 * String.format("%tD", new Date())  //03/04/11
 * String.format("%td", new Date())   //04
 */
@Component
public class DateTools {

    public static String FORMAT_yyyyMMdd = "yyyyMMdd";
    public static String FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
    public static String FORMAT_yyyyMMddHHmm = "yyyy-MM-dd HH:mm";
    public static String FORMAT_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static String FORMAT_yyyyMMddHHmmss_CH = "yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒";
    public static String TIME_000000 = "000000";
    public static String TIME_000 = "000";
    public static String TIME_235959 = "235959";
    public static long ONEDAY_TIME = 86400000L;


    public static String getCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }

    /**
     * @param date 当前时间
     * @return
     */
    public static String getCurrentDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_yyyyMMddHHmmss);
        return sdf.format(date);
    }

    public static String getCurrentDate(Date date, String format) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    @Test
    public void measureExecutionTimeTest() {
        String formattedTime = measureExecutionTime((long) 1500);
        System.out.println(formattedTime);
    }

    public static String measureExecutionTime(Long timeDifference) {

        Duration duration = Duration.ofMillis(timeDifference);

        // 提取分钟、秒、毫秒
        long minutes = duration.toMinutes();       // 总分钟数
        long seconds = duration.toSecondsPart();   // 剩余秒数（Java 9+）
        long millis = duration.toMillisPart();     // 剩余毫秒数（Java 9+）

        // 格式化输出
        String formattedTime;
        if (minutes > 0) {
            formattedTime = String.format("%d 分钟 %d 秒 %d 毫秒", minutes, seconds, millis);
        } else if (seconds > 0) {
            formattedTime = String.format("%d 秒 %d 毫秒", seconds, millis);
        } else {
            formattedTime = String.format("%d 毫秒", millis);
        }
        return formattedTime;
    }

    @Test
    public void getCurrentDateByChTest() throws InterruptedException {
        Long time = System.currentTimeMillis();
        Thread.sleep(1000);
        long timeDifference = System.currentTimeMillis() - time;
        Date date = new Date(timeDifference); // 这里假设time是相对于epoch的毫秒数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(date);
        System.out.println(formattedDate);
    }

    public static String getCurrentDateByCh(long timeStamp) {
        try {
            return dateToStr(new Date(timeStamp), FORMAT_yyyyMMddHHmmss_CH);
        } catch (Exception var4) {
            return null;
        }
    }

    public static String longToStr(long date, String format) {
        try {
            return dateToStr(new Date(date), format);
        } catch (Exception var4) {
            return null;
        }
    }

    public static Date getCurrentTimeNoSecond(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.clear(Calendar.SECOND);
        return cal.getTime();
    }


    // 字符串转时间戳
    public static long strToLong(String data, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(data).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public static String dateToStr(Date date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } catch (Exception var3) {
            return null;
        }
    }

    public static Date parseDate(String date, String format) {
        if (date != null && !date.equals("")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
//                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                return sdf.parse(date);
            } catch (Exception var3) {
                return null;
            }
        }
        return null;
    }

    // 时间转时间戳
    public static Long dateToLong(Date date) {
        try {
            return date.getTime() / 1000;
        } catch (Exception var3) {
            return null;
        }
    }

    /**
     * 时间戳转日期
     *
     * @param timestamp 时间戳
     * @param format    时间格式
     * @return
     */
    public static String longToDate(Long timestamp, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = new Date(timestamp);
            return sdf.format(date);
        } catch (Exception var3) {
            return null;
        }
    }


    // 转换 10位时间戳
    public static long getTimesTamp10() {
        Date date = new Date();
        return date.getTime() / 1000;
    }

    public static long getTimesTamp10(Date date) {
        if (date == null) {
            date = new Date();
        }
        return date.getTime() / 1000;
    }

    public static long currentTimeMillis() {
        Long currencTimeMillis = System.currentTimeMillis();
        return currencTimeMillis;
    }

    public static long currentTimeSecond() {
        Long currencTimeMillis = System.currentTimeMillis();
        return currencTimeMillis / 1000;
    }

    public static int compare(Date date1, Date date2) {
        int day = (int) ((date1.getTime() - date2.getTime()) / ONEDAY_TIME);
        return day;

    }

    public static int compare(Long time1, Long time2) {
//        int day = (int) ((time1 - time2) / ONEDAY_TIME);
//        return day;

        int day = (int) ((time1 - time2) / ONEDAY_TIME);
        if (day <= 0) {
            return 0;
        }
        return day;
    }

    public static long millisecondInterval(Long time1, Long time2) {
        long day = time1 - time2;
        long second = day / 1000;
        return second;
    }

    public static long secondInterval(Long time1, Long time2) {
        long second = time1 - time2;
        return second;
    }

    public static String uptime(Long time) {
        //获取结束时间
        Date finishTime = new Date();
        //结束时间 转为 Long 类型
        Long end = finishTime.getTime();
        // 时间差 = 结束时间 - 开始时间，这样得到的差值是毫秒级别
        long timeLag = time * 1000;
        //天
        long day = timeLag / (24 * 60 * 60 * 1000);
        //小时
        long hour = (timeLag / (60 * 60 * 1000) - day * 24);
        //分钟
        long minute = ((timeLag / (60 * 1000)) - day * 24 * 60 - hour * 60);
        //秒，顺便说一下，1秒 = 1000毫秒
        long s = (timeLag / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        System.out.println("用了 " + day + "天 " + hour + "时 " + minute + "分 " + s + "秒");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("任务结束，结束时间为：" + df.format(finishTime));
        return day + "天 " + hour + "时 " + minute + "分";
    }

    // 计算时间差
    @Test
    public void calculatingTimeDifference() throws InterruptedException {
        Calendar cal = Calendar.getInstance();
        Long start_1 = cal.getTime().getTime();

        Thread.sleep(5000);

        cal.add(Calendar.MINUTE, 1);
        cal.add(Calendar.SECOND, 1);
        Long start_2 = cal.getTime().getTime();
        Long diff = start_2 - start_1;


        long diffSeconds = diff / 1000 % 60;

        long diffMinutes = diff / (60 * 1000) % 60;

        long diffHours = diff / (60 * 60 * 1000) % 24;

        long diffDays = diff / (24 * 60 * 60 * 1000);

        System.out.println(diffSeconds);
        System.out.println(diffMinutes);
        System.out.println(diffHours);
        System.out.println(diffDays);
    }

    @Test
    public void diff() {
        long time = 1677059659000L;
        long time2 = 1677059898000L;
        long diff = time2 - time;

        long diffSeconds = diff / 1000 % 60;

        long diffMinutes = diff / (60 * 1000) % 60;

        long diffHours = diff / (60 * 60 * 1000) % 24;

        long diffDays = diff / (24 * 60 * 60 * 1000);

        System.out.println(diffSeconds);
        System.out.println(diffMinutes);
        System.out.println(diffHours);
        System.out.println(diffDays);

    }

    @Test
    public void testGetMinTime() {
        Long time = getMinTime(-1);
        System.out.println(time / 1000);


        Date date = getMinDate(-1);
        System.out.println(date);

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();

        // 将秒清零
        LocalDateTime previousMinute = currentTime.withSecond(0).minusMinutes(1);
        System.out.println(previousMinute);

    }

    // 获取前N分钟时间
    public static Long getMinTime(int min) {
        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MINUTE, Math.negateExact(min));
        cal.add(Calendar.MINUTE, min);
        return cal.getTime().getTime() / 1000;
    }

    public static Date getMinDate(int min) {
        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MINUTE, Math.negateExact(min));
        cal.add(Calendar.MINUTE, min);

        // 将秒清零
        cal.clear(Calendar.SECOND);

        return cal.getTime();
    }

    public static Date getMinDate(int min, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
//        cal.add(Calendar.MINUTE, Math.negateExact(min));
        cal.add(Calendar.MINUTE, min);

        // 将秒清零
        cal.clear(Calendar.SECOND);

        return cal.getTime();
    }


    @Test// 计算时间差
    public void testn() {
        //获取结束时间
        Date finishTime = new Date();
        //结束时间 转为 Long 类型
        Long end = finishTime.getTime();
        // 时间差 = 结束时间 - 开始时间，这样得到的差值是毫秒级别
        long timeLag = 141223 * 1000;
        //天
        long day = timeLag / (24 * 60 * 60 * 1000);
        //小时
        long hour = (timeLag / (60 * 60 * 1000) - day * 24);
        //分钟
        long minute = ((timeLag / (60 * 1000)) - day * 24 * 60 - hour * 60);
        //秒，顺便说一下，1秒 = 1000毫秒
        long s = (timeLag / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        System.out.println("用了 " + day + "天 " + hour + "时 " + minute + "分 " + s + "秒");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("任务结束，结束时间为：" + df.format(finishTime));
    }

    @Test// 计算时间差
    public void testnn() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(140554);// 格式化时间
        System.out.println(format);
    }

    // 避免精度丢失，这里设置毫秒为0

    @Test
    public void testGatherDate() {
        System.out.println(gatherDate());
    }

    public static Date gatherDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        // 清除秒和毫秒
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        return date;
    }

    // 避免精度丢失，这里设置毫秒为0（采集时写入数据库的时间，精度丢失）
    public static Date getStartOfDay() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        return startOfDay;
    }

    public static Date getEndOfDay() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        Date endOfDay = cal.getTime();
        return endOfDay;
    }


    // 获取指定日期的结束时间
    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    @Test
    public void test1() {
        System.out.println(getStartOfDay());

        System.out.println(new Date());

        for (int i = 0; i < 100; i++) {
            System.out.println(getEndOfDay());
        }

    }


    @Test
    public void getTimestampTest1(){
        System.out.println(getTimestamp());
    };
    public String getTimestamp() {
        // 获取当前时间
        Instant now = Instant.now();

        // 获取两分钟前的时间
        Instant twoMinutesAgo = now.minus(2, ChronoUnit.MINUTES);

        // 打印当前时间和两分钟前的时间
        System.out.println("Current Time: " + now);
        System.out.println("Two Minutes Ago: " + twoMinutesAgo);

        // 生成随机时间戳
//        Instant randomTimestamp = generateRandomTimestamp(twoMinutesAgo, now);

        // 获取当前时间和两分钟前时间的 13 位时间戳
        long currentTimestamp = now.toEpochMilli();
        long twoMinutesAgoTimestamp = twoMinutesAgo.toEpochMilli();

        // 打印时间戳
        System.out.println("Current Timestamp (13 digits): " + currentTimestamp);
        System.out.println("Timestamp Two Minutes Ago (13 digits): " + twoMinutesAgoTimestamp);

        // 生成随机时间戳
        long randomTimestamp = generateRandomTimestamp(twoMinutesAgoTimestamp, currentTimestamp);

        // 打印随机时间戳
        System.out.println("Random Timestamp (13 digits): " + randomTimestamp);
        return String.valueOf(randomTimestamp);
    }

    @Test
    public void getTimestampTest() {
        // 获取当前时间
        Instant now = Instant.now();

        // 获取两分钟前的时间
        Instant twoMinutesAgo = now.minus(2, ChronoUnit.MINUTES);

        // 打印当前时间和两分钟前的时间
        System.out.println("Current Time: " + now);
        System.out.println("Two Minutes Ago: " + twoMinutesAgo);

        // 生成随机时间戳
//        Instant randomTimestamp = generateRandomTimestamp(twoMinutesAgo, now);

        // 获取当前时间和两分钟前时间的 13 位时间戳
        long currentTimestamp = now.toEpochMilli();
        long twoMinutesAgoTimestamp = twoMinutesAgo.toEpochMilli();

        // 打印时间戳
        System.out.println("Current Timestamp (13 digits): " + currentTimestamp);
        System.out.println("Timestamp Two Minutes Ago (13 digits): " + twoMinutesAgoTimestamp);

        // 生成随机时间戳
        long randomTimestamp = generateRandomTimestamp(twoMinutesAgoTimestamp, currentTimestamp);

        // 打印随机时间戳
        System.out.println("Random Timestamp (13 digits): " + randomTimestamp);
//        return randomTimestamp.toString();
    }

    /**
     * 在指定时间范围内生成一个随机时间戳
     *
     * @param start 起始时间戳
     * @param end   结束时间戳
     * @return 随机生成的时间戳
     */
    public static long generateRandomTimestamp(long start, long end) {
        if (start > end) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Random random = new Random();
        return start + (long) (random.nextDouble() * (end - start));
    }

    /**
     * 生成一个在指定时间范围内的随机时间戳
     *
     * @param start 起始时间
     * @param end   结束时间
     * @return 随机生成的时间戳
     */
    public static Instant generateRandomTimestamp(Instant start, Instant end) {
        // 计算起始时间和结束时间的秒数差
        long startEpochSecond = start.getEpochSecond();
        long endEpochSecond = end.getEpochSecond();
        long randomEpochSecond = startEpochSecond + new Random().nextLong() % (endEpochSecond - startEpochSecond);

        // 返回生成的随机时间戳
        return Instant.ofEpochSecond(randomEpochSecond);
    }
}
