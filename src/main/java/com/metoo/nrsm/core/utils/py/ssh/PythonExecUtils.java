package com.metoo.nrsm.core.utils.py.ssh;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.utils.Global;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-29 10:11
 */
@Slf4j
@Component
public class PythonExecUtils implements InitializingBean {

    @Autowired
    private SSHExecutor sshExecutor;


    public static void main(String[] args) throws IOException {
        String[] args1 = new String[]{
                "python", "E:\\python\\project\\djangoProject\\app01\\nrsm\\getarpv6.py", null};
        Process proc = Runtime.getRuntime().exec(args1);
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文

        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(sb);
    }

    /**
     * 变量被关键字static修饰
     * 类没有使用@Component及其衍生标签修饰
     * 在Bean初始化时构造方法中引用被@Value修饰的变量
     *
     * @value 在拦截器配置中
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public String exec(String path) {
        String py_version = Global.py_name;
        if("dev".equals(Global.env)){
            return sshExecutor.exec(path);
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = new String[]{
                    py_version, "-W", "ignor", path};
            Process proc = Runtime.getRuntime().exec(args);
//            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                sb.append(line);
//            }
//            in.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream(), "UTF-8"));

// 读取标准输出
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                log.info("============= 输出" + line);
                System.out.println("输出: " + line);
            }

// 读取错误输出
            while ((line = err.readLine()) != null) {
                log.info("============= 错误" + line);
                System.out.println("错误: " + line);
            }
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.info("============= " + sb.toString());
        return sb.toString();
    }

    public String exec(String path, String[] params) {
        String py_version = Global.py_name;;
        if("dev".equals(Global.env)){
            return sshExecutor.exec(path, params);
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = new String[]{
                    py_version, "-W", "ignor", path};
            Process proc = null;
            String[] filteredArray = null;
            if (params.length > 0) {
                filteredArray = Arrays.stream(params)
                        .filter(s -> s != null)
                        .toArray(String[]::new);
            }

            if(filteredArray != null && filteredArray.length > 0){

                String[] mergedArray = new String[args.length + filteredArray.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = filteredArray[i - argsLen];
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
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));
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

    public String exec(String path, String[] params, String prefix) {
        String py_version = Global.py_name;
        if("dev".equals(Global.env)){
            return sshExecutor.exec(path, params, prefix);
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = null;
            if(StringUtil.isNotEmpty(prefix)){
                args = new String[]{
                        prefix, py_version, "-W", "ignor", path};
            }else{
                args = new String[]{
                        py_version, "-W", "ignor", path};
            }

            Process proc = null;
            String[] filteredArray = null;
            if (params.length > 0) {
                filteredArray = Arrays.stream(params)
                        .filter(s -> s != null)
                        .toArray(String[]::new);
            }

            if(filteredArray != null && filteredArray.length > 0){

                String[] mergedArray = new String[args.length + filteredArray.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = filteredArray[i - argsLen];
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
                // 检查子进程是否正常启动
//                InputStream inputStream = proc.getInputStream();
//                InputStream errorStream = proc.getErrorStream();

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

    public String execNohup(String path, String[] params, String prefix) {
        String py_version = Global.py_name;
        if("dev".equals(Global.env)){
            return sshExecutor.exec(path, params, prefix);
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = null;
            if(StringUtil.isNotEmpty(prefix)){
                args = new String[]{
                        prefix, py_version, "-W", "ignor", path};
            }else{
                args = new String[]{
                        py_version, "-W", "ignor", path};
            }

            ProcessBuilder processBuilder = null;

            String[] filteredArray = null;
            if (params.length > 0) {
                filteredArray = Arrays.stream(params)
                        .filter(s -> s != null)
                        .toArray(String[]::new);
            }

            if(filteredArray != null && filteredArray.length > 0){

                String[] mergedArray = new String[args.length + filteredArray.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = filteredArray[i - argsLen];
                    }
                }
                try {
                    processBuilder = new ProcessBuilder(mergedArray);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    processBuilder = new ProcessBuilder(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(processBuilder != null){
                Process proc = processBuilder.start();
                proc.waitFor();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public void execNohup() {
        ProcessBuilder processBuilder = new ProcessBuilder("nohup", "python3", "/opt/nrsm/py/Dnsredis.py", "0", "&");
        processBuilder.redirectErrorStream(true); //将错误输出重定向到标准输出
        try {
            Process process = processBuilder.start();
            process.waitFor(); //等待进程执行完成
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String exec2(String path, String[] params) {
        String py_version = Global.py_name;
        if("dev".equals(Global.env)){
            return sshExecutor.exec(path, params);
        }
        StringBuffer sb = new StringBuffer();
        try {
            String[] args = new String[]{
                    py_version, "-W", "ignor", path};
            Process proc = null;
            String[] filteredArray = null;
            if (params.length > 0) {
                filteredArray = Arrays.stream(params)
                        .filter(s -> s != null)
                        .toArray(String[]::new);
            }

            if(filteredArray != null && filteredArray.length > 0){

                String[] mergedArray = new String[args.length + filteredArray.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = filteredArray[i - argsLen];
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
