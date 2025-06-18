package com.metoo.nrsm.core.wsapi.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.entity.Terminal;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Md5Crypt {

    public static String md5(String str){
        Md5Hash md5Hash = new Md5Hash(str);
        return md5Hash.toHex();
    }

    private static boolean getDiffrent(List<Terminal> list, List<Terminal> list1) {
        long st = System.currentTimeMillis();
        /** 使用Security的md5方法进行加密 */
        String str = md5(list.toString());
        String str1 = md5(list1.toString());
        System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
        return str.equals(str1);
    }

    public static boolean getDiffrent(Object o1, Object o2) {
        long st = System.currentTimeMillis();
        /** 使用Security的md5方法进行加密 */
        if(o1 instanceof ArrayList && o2 instanceof ArrayList) {
            String str = md5(o1.toString());
            String str1 = md5(o2.toString());
            System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
            return str.equals(str1);
        }
        String str = md5(JSONObject.toJSONString(o1));
        String str1 = md5(JSONObject.toJSONString(o2));
        System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
        return str.equals(str1);
    }


    public static boolean getDiffrentStr(Object o1, Object o2) {
        long st = System.currentTimeMillis();
        /** 使用Security的md5方法进行加密 */
        String str = md5(o1.toString());
        String str1 = md5(o2.toString());
        System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
        return str.equals(str1);
    }

    // ==============================================================


    // 使用该方法替换
    public static boolean getDiffrentOptimize(Object o1, Object o2) {
        // 如果两个对象引用相同或内容相等，直接返回
        if (o1 == o2) {
            return true;
        }

        // 如果对象类型是ArrayList，直接对toString()进行加密处理
        String hash1 = getObjectHash(o1);
        String hash2 = getObjectHash(o2);

        boolean result = hash1.equals(hash2);

        return result;
    }

    private static String getObjectHash(Object object) {
        // 对所有对象使用 JSON 序列化
        return md5Optimize(JSONObject.toJSONString(object));
    }

    private static String md5Optimize(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static void main(String[] args) {
        List<Map<String, String>> list1 = new ArrayList<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("key1", "value1");
        map1.put("key2", "value2");
        list1.add(map1);

        List<Map<String, String>> list2 = new ArrayList<>();
        Map<String, String> map2 = new HashMap<>();
        map2.put("key1", "value1");
        map2.put("key2", "value2");
        list2.add(map2);

        // 应该返回 true，因为内容相同
        System.out.println(getDiffrent(list1, list2));

        // 测试不同的内容
        Map<String, String> map3 = new HashMap<>();
        map3.put("key1", "value3");
        list2.add(map3);

        // 应该返回 false，因为内容不同
        System.out.println(getDiffrent(list1, list2));
    }
}
