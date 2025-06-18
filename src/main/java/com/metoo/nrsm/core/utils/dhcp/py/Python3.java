package com.metoo.nrsm.core.utils.dhcp.py;


import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-25 16:52
 */
public class Python3 {


    public static void main(String[] params) {

        String[] args = new String[] {
                "python3", "/opt/nrsm/py/modifyip.py"};

        String[] mergedArray = new String[args.length + params.length];

        int argsLen = args.length;

        for (int i = 0; i < mergedArray.length; i++) {

            if(i < argsLen){
                mergedArray[i] = args[i];
            }else{
                mergedArray[i] = params[i - argsLen];
            }
        }

        Process proc = null;// 执行py文件
        StringBuffer sb = new StringBuffer();
        try {
            proc = Runtime.getRuntime().exec(args);

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb);
            if(sb.toString().equals("0")){
                System.out.println("true");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("false");
    }

    public static void main2(String[] args) {

        System.out.println(args[0]);
        System.out.println(args[1]);
        String[] array1 = {"Hello", "World"};
        String[] array2 = {"a", "b"};

        // 创建新的字符串数组，长度为array1.length + array2.length
        String[] mergedArray = new String[array1.length + array2.length];

        // 复制array1到mergedArray中
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);

        // 复制array2到mergedArray中
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);

        // 输出合并后的结果
        for (int i = 0; i < mergedArray.length; i++) {
//            System.out.print(mergedArray[i] + " ");
        }



        String[] array3 = {"Hello", "World"};
        array3[0] = "q";
        for (int i = 0; i < array3.length; i++) {
            System.out.print(array3[i] + " ");
        }
    }

}
