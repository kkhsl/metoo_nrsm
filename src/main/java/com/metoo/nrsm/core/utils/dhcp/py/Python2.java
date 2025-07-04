package com.metoo.nrsm.core.utils.dhcp.py;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.entity.Interface;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-25 17:24
 */
public class Python2 {

    @Test
    public void test() {
        StringBuffer sb = new StringBuffer();
        sb.append(0);
        StringBuffer sb2 = new StringBuffer();
        if (sb.toString().equals("0")) {
            System.out.println(1);
        } else {
            System.out.println(0);
        }

    }

    public static void main(String[] args) {
        try {
//            String[] args1 = new String[] {
//                    "python", "C:\\Users\\Administrator\\Desktop\\q\\getarp.py"};
//
//            String[] args1 = new String[] {
//                    "python", "C:\\Users\\Administrator\\Desktop\\q\\getarp.py"};

            String[] args1 = new String[]{
                    "python", "E:\\python\\project\\djangoProject\\app01\\TestAbstrack.py"};
//            String[] args1 = new String[] {
//                    "python3", "/opt/nrsm/py/getnetintf.py"};

            String[] mergedArray = new String[args1.length + args.length];

            int args1Len = args1.length;

            for (int i = 0; i < mergedArray.length; i++) {

                if (i < args1Len) {
                    mergedArray[i] = args1[i];
                } else {
                    mergedArray[i] = args[i - args1Len];
                }
            }

            Process proc = Runtime.getRuntime().exec(mergedArray);// 执行py文件

            StringBuffer sb = new StringBuffer();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
//                System.out.println(line);
                sb.append(line);
            }

            if (sb.equals("0")) {
                System.out.println(1);
            } else {
                System.out.println(0);
            }
            List list = new ArrayList<>();
            Map<String, Object> map = JSONObject.parseObject(sb.toString(), Map.class);

            for (String key : map.keySet()) {
//                System.out.println("key: " + key + " value: " + map.get(key));
                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
                inteface.setName(key);
                list.add(inteface);
            }


//            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(entry), Interface.class);
//                inteface.setName(entry.getKey());
//                list.add(inteface);
//            }

            list.forEach(e -> {
                System.out.println(e);
            });


            in.close();
            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void getdhcp() {
        String[] args = new String[]{
                "python", "E:\\python\\project\\djangoProject\\app01\\nrsm\\getdhcp.py"};
        Process proc = null;// 执行py文件
        StringBuffer sb = new StringBuffer();
        try {
            proc = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb);
            JSONArray array = JSONObject.parseArray(JSONObject.toJSONString(sb));
            if (array.size() > 0) {
                System.out.println(JSONObject.toJSONString(array.get(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getdns() {
        String[] args = new String[]{
                "python", "E:\\python\\project\\djangoProject\\app01\\nrsm\\getdns.py"};
        Process proc = null;// 执行py文件
        StringBuffer sb = new StringBuffer();
        try {
            proc = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
