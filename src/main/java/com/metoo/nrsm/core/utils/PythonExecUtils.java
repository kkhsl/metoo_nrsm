package com.metoo.nrsm.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.entity.nspm.Interface;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-29 10:11
 */
@Component
public class PythonExecUtils implements InitializingBean {

    /**
     * 变量被关键字static修饰
     * 类没有使用@Component及其衍生标签修饰
     * 在Bean初始化时构造方法中引用被@Value修饰的变量
     *
     * @value 在拦截器配置中
     */

    private static String env;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println();
    }

    @Value("${spring.profiles.active}")
    public void setUrl(String env) {
        PythonExecUtils.env = env;
    }


    public static String exec(String path) {
        String py_version = "python";
        if (env.equals("prod")) {
            py_version = "python3";
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = new String[]{
                    py_version, path};

            Process proc = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String exec(String path, String[] params) {
        String py_version = "python";
        if (env.equals("prod")) {
            py_version = "python3";
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = new String[]{
                    py_version, path};
            Process proc = null;
            if (params.length > 0) {
                String[] mergedArray = new String[args.length + params.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = params[i - argsLen];
                    }
                }
                try {
                    proc = Runtime.getRuntime().exec(mergedArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    proc = Runtime.getRuntime().exec(args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(proc != null){
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
                String line = null;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                proc.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
