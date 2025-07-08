package com.metoo.nrsm.core.utils.collections;

import com.metoo.nrsm.entity.Interface;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-30 11:25
 */
public class Test {

    public static void main(String[] args) {
        String port = "GigabitEthernet1/0/4";
        String o1_num = port.replaceAll("[a-zA-Z]", "");
        o1_num = o1_num.replaceAll("/", "");
        int i = Integer.parseInt(o1_num);


        String o1_str = port.replaceAll("[0-9]", "");
        o1_str = o1_str.replaceAll("/", "");

    }

    @org.junit.Test
    public void test() {
        List<Integer> list = new ArrayList();

        list.add(104);
        list.add(100);
        list.add(102);
        list.add(101);
        list.add(103);
        Collections.sort(list);
    }

    @org.junit.Test
    public void test2() {
        List<String> list = new ArrayList();

        list.add("GigabitEthernet1/0/4");
        list.add("GigabitEthernet1/0/33");
        list.add("GigabitEthernet1/0/3");
        list.add("GigabitEthernet1/0/0");
        list.add("GigabitEthernet1/0/2");
        list.add("InLoopBack0");
        list.add("Ethernet0/0/6");
        list.add("GigabitEthernet1/0/1");
        list.add("Ethernet0/0/2");

//        Collections.sort(list);// 字符串排序
//
//        System.out.println(list);

        Collections.sort(list, (o1, o2) -> compareString(o1, o2));


    }

    /**
     * 包含数字的字符串进行比较（按照从小到大排序）
     */
    private static Integer compareString(String string1, String string2) {
        //拆分两个字符串
        List<String> list1 = splitString(string1);
        List<String> list2 = splitString(string2);
        //依次对比拆分出的每个值
        int index = 0;
        while (true) {
            //相等表示两个字符串完全相等
            if (index >= Math.max(list1.size(), list2.size())) {
                return 0;
            }
            String str1 = null;
            if (index < list1.size()) {
                str1 = list1.get(index);
            } else {
                str1 = "";
            }
            String str2 = null;
            if (index < list2.size()) {
                str2 = list2.get(index);
            } else {
                str2 = "";
            }
            //字符串相等则继续判断下一组数据
            if (str1.equals(str2)) {
                index++;
                continue;
            }
            //是纯数字，比较数字大小
            if (isNum(str1) && isNum(str2)) {
                if (Integer.parseInt(str1) < Integer.parseInt(str2)) {
                    return -1;
                } else {
                    return 1;
                }
            }
            // 字符串比较大小
            if (str1.compareTo(str2) > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /**
     * 拆分字符串
     * 输入：第5章第100节课
     * 返回：[第,5,章第,100,节课]
     */
    private static List<String> splitString(String str) {
        Matcher matcher = Pattern.compile("([^0-9]+)|(\\d+)").matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 是否是纯数字
     */
    private static Boolean isNum(String str) {
        return Pattern.compile("\\d+").matcher(str).matches();
    }


}
