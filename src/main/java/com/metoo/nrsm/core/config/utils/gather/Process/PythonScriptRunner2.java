package com.metoo.nrsm.core.config.utils.gather.Process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 11:40
 */
@Slf4j
@Component
public class PythonScriptRunner2 {

    /**
     *
     * @param scriptPath
     * @param args
     * @return
     */
    public String exec(String scriptPath, String... args) {
        try {
            // 构建命令行参数，包括 Python 解释器和脚本路径
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);

            // 设置工作目录
            pb.directory(new File(scriptPath));

            pb.command().addAll(Arrays.asList(args));

            Process process = pb.start();

            StringBuilder output = new StringBuilder();

            // 创建线程读取标准输出流
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 启动线程
            outputThread.start();

            // 等待进程完成
            try {
                int exitCode = process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // 等待线程完成
            try {
                outputThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 返回执行结果
            String cleanedOutput = output.toString().replaceAll("[\n\r]", "");
            return cleanedOutput;

        } catch (IOException e) {
            e.printStackTrace();
            return ""; // 或者抛出异常，根据需求处理
        }
    }

}
