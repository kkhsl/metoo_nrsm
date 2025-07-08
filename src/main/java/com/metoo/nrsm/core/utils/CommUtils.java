package com.metoo.nrsm.core.utils;

import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.entity.User;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Component
public class CommUtils {

    Logger logger = LoggerFactory.getLogger(CommUtils.class);

    private static final SimpleDateFormat dateFormat = new

            SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 24小时制

    public static void main(String[] args) {
    }


    public static final String randomString(int length) {
        char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                .toCharArray();
        if (length < 1) {
            return "";
        }
        Random randGen = new Random();
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];// nextInt:这里是一个方法的重载，参数的内容是指定范围
        }
        return new String(randBuffer);
    }

    public static String formatTime(String format, Object v) {
        if (v == null) {
            return null;
        }
        if (format.equals("")) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(v);
    }

    public static Date formatDate(String s) {
        Date d = null;
        try {
            d = dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }


    public static String getRtmp(String ip, String bindCode) {
        if (ip != null && !ip.equals("")) {
            if (bindCode != null && !bindCode.equals("")) {
                String rtmp = "httpclient://" + ip + "/hls/" + bindCode;
                return rtmp;
            }
        }
        return null;
    }

    public static String getObsRtmp(String ip) {
        if (ip != null && !ip.equals("")) {
            String rtmp = "rtmp://" + ip + "/hls";
            return rtmp;
        }
        return null;
    }


    /**
     * 修改目录权限
     *
     * @param dirPath 目录
     * @param value   权限值
     */
    public static String filePermisession(String dirPath, String value) {
        Runtime runtime = Runtime.getRuntime();
        String command = "chmod " + value + dirPath;
        try {
            Process process = runtime.exec(command);
            process.waitFor();

            int existValue = process.exitValue();
            if (existValue != 0) {
                System.out.println("Change file permission failed");
                return "Change file permission failed " + existValue;
            }
            return "Successfully";
        } catch (Exception e) {
            return "Command execute failed";
        }
    }

    public static String appointedDay(Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.HOUR_OF_DAY, 0);
        calendar.set(calendar.MINUTE, 0);
        calendar.set(calendar.SECOND, 0);
        calendar.add(calendar.DATE, day);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    /**
     * 获取当前用户ID
     *
     * @param map
     */
    public static void currentUserId(Map map) {
        User user = ShiroUserHolder.currentUser();
        if (user.getUserRole().equals("SUPPER")) {
            map.put("admin", user.getId());
        }
        map.put("userId", user.getId());
    }

/*    public static  Object removeUserSessionByUser(User user){
        List<Object> users = sessionRegistry.getAllPrincipals(); // 获取session中所有的用户信息
    }*/


    /**
     * 生成加密密码
     *
     * @param password
     * @return
     */
    public static String password(String password, String sale) {

        // 明文密码进行 md5 + salt + hash散列
        Md5Hash md5Hash = new Md5Hash(password, sale, 1024);
        return md5Hash.toHex();
    }
}
