package com.metoo.nrsm.core.wsapi.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.entity.Terminal;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Md5Crypt {

    public static String md5(String str, String salt){
        Md5Hash md5Hash = new Md5Hash(str, salt);
        return md5Hash.toHex();
    }

    private static boolean getDiffrent(List<Terminal> list, List<Terminal> list1) {
        long st = System.currentTimeMillis();
        /** 使用Security的md5方法进行加密 */
        String str = md5(list.toString(), "aa");
        String str1 = md5(list1.toString(), "aa");
        System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
        return str.equals(str1);
    }

    public static boolean getDiffrent(Object o1, Object o2) {
        long st = System.currentTimeMillis();
        /** 使用Security的md5方法进行加密 */
        if(o1 instanceof ArrayList && o2 instanceof ArrayList) {
            String str = md5(o1.toString(), "aa");
            String str1 = md5(o2.toString(), "aa");
            System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
            return str.equals(str1);
        }
        String str = md5(JSONObject.toJSONString(o1), "aa");
        String str1 = md5(JSONObject.toJSONString(o2), "aa");
        System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
        return str.equals(str1);
    }


    public static boolean getDiffrentStr(Object o1, Object o2) {
        long st = System.currentTimeMillis();
        /** 使用Security的md5方法进行加密 */
        String str = md5(o1.toString(), "aa");
        String str1 = md5(o2.toString(), "aa");
        System.out.println("消耗时间为： " + (System.currentTimeMillis() - st));
        return str.equals(str1);
    }

}
