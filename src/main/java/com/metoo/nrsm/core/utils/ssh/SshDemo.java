package com.metoo.nrsm.core.utils.ssh;


import org.omg.SendingContext.RunTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-05 14:56
 */
public class SshDemo {

    public static void main(String[] args){
        try {
            Process proc = Runtime.getRuntime().exec("python3 /opt/nrsm/py/getportmac.py 192.168.5.205 v2c public@123");
            // 标准输入流（必须写在 waitFor 之前）
            String inStr = consumeInputStream(proc.getInputStream());
            // 标准错误流（必须写在 waitFor 之前）
            String errStr = consumeInputStream(proc.getErrorStream());

            int retCode = proc.waitFor();
            if(retCode == 0){
                System.out.println("程序正常执行结束");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    /**
     *   消费inputstream，并返回
     */
    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s ;
        StringBuilder sb = new StringBuilder();
        while((s=br.readLine())!=null){
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }

}
