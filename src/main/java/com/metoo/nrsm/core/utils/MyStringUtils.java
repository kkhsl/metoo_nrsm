package com.metoo.nrsm.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@Component
public class MyStringUtils {


    /**
     * 1：判断字符是否为数字或字母
     * @return
     */

    public static void main(String[] args) {
//        String value = "f1234";
//        String value1 = "abasd";
//        // 方式一：
//        boolean flag = isInteger(value);
//        boolean flag1 = isInteger(value1);
//        System.out.println(flag);
//        System.out.println(flag1);
//
//        boolean isDigitInt = isDigit(value);
//        boolean isDigitStr = isDigit(value1);
//        System.out.println(isDigitInt);
//        System.out.println(isDigitStr);

        String param = "192.168.5.1";
        String symbol = "\\.";
//        Pattern pattern = Pattern.compile(symbol);
//        Matcher findMatcher = pattern.matcher(param);
//        List<Integer> list = new ArrayList();
//        while(findMatcher.find()) {
//            list.add(findMatcher.start());
//        }
        int a = acquireCharacterPositions(param, symbol, 3);
        System.out.println(a);

    }

    // 方式一：
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Test
    public void idd(){
        String value = "ewa";
        System.out.println(value.charAt(0));
    }

    // 方式二：Character
    public static boolean isDigit(String value){

        for (int i = value.length(); --i >= 0;){
            System.out.println(value.charAt(i));
            if (!Character.isDigit(value.charAt(i))){

                return false;
            }

        }
        return true;
    }

    public static String getStr(String s) {
        if (s == null) {
            return "";
        }
        if (s.isEmpty()) {
            return s;
        }
        return s.trim();
    }

    /**
     * 测试指定字符串出现的位置
     */
    public static void acquireCharacterPosition() {
        int i = acquireCharacterPositions("00", ":", 1);
        System.out.println(i);
    }


    /**
     * 获取指定字符第N次出现的位置(下标)
     * @return
     */
    public static int acquireCharacterPositions(String param, String symbol, int num){
        if(StringUtils.isBlank(param)){
            return -1;
        }
        boolean flag = false;
        if(symbol.equals(".")){
            flag = true;
        }
        if(param.contains(symbol) && param.indexOf(symbol) != -1){
            flag = true;
        }
        if(flag){
            Pattern pattern = Pattern.compile(symbol);
            Matcher findMatcher = pattern.matcher(param);
            List<Integer> list = new ArrayList();
            while(findMatcher.find()) {
                list.add(findMatcher.start());
            }
            return list.get(num - 1);
        }
        return -1;
    }

    /**
     * 获取指定字符串出现的次数
     *
     * @param srcText 源字符串
     * @param findText 要查找的字符串
     * @return
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    @Test
    public void t(){
        System.out.println("输出整型数组:");
        Integer[] integerArray = { 1, 2, 3, 4, 5, 6 };
        for (Integer element : integerArray){
            System.out.printf("%s ", element);
            System.out.println();
        }

    }
    static  class MainClass{
        public static void main(String[] args) {
            int nDisks = 4;
            doTowers(nDisks, 'A', 'B', 'C');
        }
        public static void doTowers(int topN, char from, char inter, char to) {
            if (topN == 1){
                System.out.println("Disk 1 from "
                        + from + " to " + to);
            }else {
                doTowers(topN - 1, from, to, inter);
                System.out.println("Disk "
                        + topN + " from " + from + " to " + to);
                doTowers(topN - 1, inter, from, to);
            }
        }
    }

    @Test
    public void str2listTest() {
        String s = "a\nb";
        List<String> c = null;
        try {
            c = str2list(s);
            System.out.println(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用来处理一行一条数据
     *
     * @param s
     * @return
     * @throws IOException
     */
    public static List<String> str2list(String s) throws IOException {
        List<String> list = new ArrayList<String>();
        s = null2String(s);
        if (!s.equals("")) {
            StringReader fr = new StringReader(s);
            BufferedReader br = new BufferedReader(fr);
            String aline = "";
            while ((aline = br.readLine()) != null) {
                list.add(aline);
            }
        }
        return list;
    }

    public static String null2String(Object s) {
        return s == null ? "" : s.toString().trim();
    }

}
