package com.metoo.nrsm.core.utils.collections;

import com.metoo.nrsm.entity.Port;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ListSortUtil {

    public static void compareTo(List<Map<Object, Object>> list){
        Collections.sort(list, new Comparator<Map<Object, Object>>() {
            @Override
            public int compare(Map<Object, Object> o1, Map<Object, Object> o2) {
                String key1 = o1.get("policyCheckTotal").toString();
                String key2 = o2.get("policyCheckTotal").toString();
                return key2.compareTo(key1);
            }
        });
    }

    public static void sort(List<Map<String, Double>> list){
        Collections.sort(list, new Comparator<Map<String, Double>>() {
            @Override
            public int compare(Map<String, Double> o1, Map<String, Double> o2) {
                Double key1 = o1.get("grade");
                Double key2 = o2.get("grade");
                return key1.compareTo(key2);
            }
        });
    }

    /**
     * 属性：（英文+数字） 组合排序
     * @param list
     */
    public static void sortStr(List<Map<String, Object>> list){
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String key1 = o1.get("name").toString();

                key1 = key1.replaceAll("/", "");
                key1 = key1.replaceAll("-", "");

                String o1_num = key1.replaceAll("[a-zA-Z]", "");

                key1 = key1.replaceAll("\\.", "");
                String o1_str = key1.replaceAll("[0-9]", "");

                String key2 = o2.get("name").toString();

//                key2 = key2.replaceAll("[a-zA-Z]", "");
                key2 = key2.replaceAll("/", "");
                key2 = key2.replaceAll("-", "");

                String o2_num = key2.replaceAll("[a-zA-Z]", "");

                key2 = key2.replaceAll("\\.", "");
                String o2_str = key2.replaceAll("[0-9]", "");

                int n = 0;
                if(!o1_str.equals(o2_str)){
                    n = o1_str.compareTo(o2_str);
                }
                if(o1_num.equals("") || o2_num.equals("")){
                    return n;
                }
                double i = Double.parseDouble(o1_num);
                double j = Double.parseDouble(o2_num);
                if(n == 0){
                    int m = i > j ? 1:-1;
                    return m;
                }else{
                    return n;
                }
            }
        });
    }

    /**
     * 属性：（英文+数字） 组合排序
     * @param list
     */
    public static void sortStr2(List<Port> list){
        Collections.sort(list, new Comparator<Port>() {
            @Override
            public int compare(Port o1, Port o2) {
                String key1 = o1.getPort();

                key1 = key1.replaceAll("/", "");
                key1 = key1.replaceAll("-", "");

                String o1_num = key1.replaceAll("[a-zA-Z]", "");

                key1 = key1.replaceAll("\\.", "");
                String o1_str = key1.replaceAll("[0-9]", "");

                String key2 = o2.getPort();

//                key2 = key2.replaceAll("[a-zA-Z]", "");
                key2 = key2.replaceAll("/", "");
                key2 = key2.replaceAll("-", "");

                String o2_num = key2.replaceAll("[a-zA-Z]", "");

                key2 = key2.replaceAll("\\.", "");
                String o2_str = key2.replaceAll("[0-9]", "");

                int n = 0;
                if(!o1_str.equals(o2_str)){
                    n = o1_str.compareTo(o2_str);
                }
                if(o1_num.equals("") || o2_num.equals("")){
                    return n;
                }
                double i = Double.parseDouble(o1_num);
                double j = Double.parseDouble(o2_num);
                if(n == 0){
                    int m = i > j ? 1:-1;
                    return m;
                }else{
                    return n;
                }
            }
        });
    }

    public static void intSort(List<Map<String, Object>> list){
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                int key1 = Integer.parseInt(o1.get("level").toString());
                int key2 = Integer.parseInt(o2.get("level").toString());
                return key1 < key2 ? 1 : -1; // 降序
            }
        });
    }



    public static void main(String[] args) {

        List<String> list = new ArrayList();
        list.add("Port1");
        list.add("Port3");
        list.add("Port2");
//        list.add("G-E100");
//        list.add("GE103");
//        list.add("GE101");
//        list.add("GES1012");
//        list.add("GsES101");
//        list.add("G00");
//        list.add("GE000");
//        list.add("GE1021");
//        list.add("GE102");
        ListSortUtil.sortStr1(list);
        System.out.println(list);
    }

    public static void sortStr1(List<String> list){
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1,String o2) {

                o1 = o1.replaceAll("/", "");

                String o1_num = o1.replaceAll("[a-zA-Z]", "");

                String o1_str = o1.replaceAll("[0-9]", "");

                o2 = o2.replaceAll("/", "");

                String o2_num = o2.replaceAll("[a-zA-Z]", "");

                String o2_str = o2.replaceAll("[0-9]", "");

                int n = 0;
                if(!o1_str.equals(o2_str)){
                    n = o2_str.length() - o1_str.length();
                }
                if(o1_num.equals("")){

                }
                if(o2_num.equals("")){

                }
                int i = Integer.parseInt(o1_num);

                int j = Integer.parseInt(o2_num);
                int m = 0;
                if(n == 0){
                    m = i > j ? 1:-1;
                    return m;
                }else{
                    return n;
                }


//                int i = Integer.parseInt(key1);

//                int j = Integer.parseInt(key2);

//                return i > j ? 1:-1;//这里返回的值，1升序 -1降序
            }
        });
    }

//    public static void sortByIp(List<Arp> list){
//        Collections.sort(list, new Comparator<Arp>() {
//            @Override
//            public int compare(Arp arp1, Arp arp2) {
//                String ip1 = arp1.getIp();
//                String ip2 = arp2.getIp();
//                return key1.compareTo(key2);
//            }
//        });
//    }

    public static void lambdaSort(List<Map<String, Double>> list){
        Collections.sort(list, (s1, s2) -> s1.get("grade").compareTo(s2.get("grade")));
    }



    public static Integer compareString(String string1, String string2) {
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
            if (index < list1.size()){
                str1 = list1.get(index);
            }else{
                str1 =  "";
            }
            String str2 = null;
            if (index < list2.size()){
                str2 = list2.get(index);
            }else{
                str2 =  "";
            }
            //字符串相等则继续判断下一组数据
            if (str1.equals(str2)) {
                index++;
                continue;
            }
            //是纯数字，比较数字大小
            if (isNum(str1) && isNum(str2)) {
                if(Integer.parseInt(str1) < Integer.parseInt(str2)){
                    return -1;
                }else{
                    return 1;
                }
            }
            // 字符串比较大小
            if(str1.compareTo(str2)>0){
                return -1;
            }else{
                return 1;
            }
        }
    }

    /**
     * 拆分字符串
     * 输入：第5章第100节课
     * 返回：[第,5,章第,100,节课]
     */
    private static List<String> splitString(String str){

        String pattern = "([^0-9]+)|(\\d+)";
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }
    /**
     * 是否是纯数字
     */
    private static Boolean isNum(String str){
        String pattern = "\\d+";
        return Pattern.compile(pattern).matcher(str).matches();
    }


}
