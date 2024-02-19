package com.metoo.nrsm.core.manager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RequestMapping("/admin")
@RestController
public class TestController {

    @GetMapping("/test")
    public Object test(){
        return "test";
    }


    public static void main(String[] args) {
        try {
            //第二个为python脚本所在位置，后面的为所传参数（得是字符串类型）

            String[] args1 = new String[] {
                    "python", "E:\\python\\project\\djangoProject\\app01\\test.py"};

            // 创建新的字符串数组，长度为array1.length + array2.length
            String[] mergedArray = new String[args1.length + args.length];


            int args1Len = args1.length;

            for (int i = 0; i < mergedArray.length; i++) {

                if(i < args1Len){
                    mergedArray[i] = args1[i];
                }else{
                    mergedArray[i] = args[i - args1Len];
                }
            }




            Process proc = Runtime.getRuntime().exec(mergedArray);// 执行py文件


//            Process proc = Runtime.getRuntime().exec(args1);// 执行py文件


//            String[] args1 = new String[] {
//                    "python", "E:\\python\\project\\djangoProject\\app01\\test.py"};
//            Process proc2 = Runtime.getRuntime().exec("\\n");// 执行回车
//            proc2.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"gb2312"));//解决中文乱码，参数可传中文
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
}
