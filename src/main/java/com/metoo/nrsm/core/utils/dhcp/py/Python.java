package com.metoo.nrsm.core.utils.dhcp.py;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-25 16:52
 */
public class Python {


    public static void main(String[] args) {
        try {
            //第二个为python脚本所在位置，后面的为所传参数（得是字符串类型）

            String[] args1 = new String[]{
                    "python", "E:\\python\\project\\djangoProject\\app01\\TestAbstrack.py"};

            // 创建新的字符串数组，长度为array1.length + array2.length
            String[] mergedArray = new String[args1.length + args.length];


            // 复制args到mergedArray中
            System.arraycopy(args1, 0, mergedArray, 0, args1.length);

            // 复制array1到mergedArray中
            System.arraycopy(args, 0, mergedArray, args.length, args.length);


            Process proc = Runtime.getRuntime().exec(mergedArray);// 执行py文件


//            Process proc = Runtime.getRuntime().exec(args1);// 执行py文件


//            String[] args1 = new String[] {
//                    "python", "E:\\python\\project\\djangoProject\\app01\\TestAbstrack.py"};
//            Process proc2 = Runtime.getRuntime().exec("\\n");// 执行回车
//            proc2.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

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
