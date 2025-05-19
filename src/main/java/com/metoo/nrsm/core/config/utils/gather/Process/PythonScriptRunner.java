package com.metoo.nrsm.core.config.utils.gather.Process;

import com.metoo.nrsm.core.config.utils.gather.common.PyCommand;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 11:40
 */
@Slf4j
@Component
public class PythonScriptRunner {

    @Test
    public void test(){
        PyCommand pyCommand = new PyCommand();
        pyCommand.setName("main.py");
        pyCommand.setParams(new String[]{"h3c", "switch", "192.168.100.1", "ssh", "22", "metoo", "metoo89745000", "aliveint"});
        List command = pyCommand.toArray();

        String result = this.runPythonScript(command);

        System.out.println(result);;
    }

    public String runPythonScript(String... args) {
        try {
            // 构建命令行参数，包括 Python 解释器和脚本路径
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.directory(new File("/opt/sqlite/script/"));

            // 将参数添加到命令行参数中
            pb.command().addAll(Arrays.asList(args));

            // 启动进程并等待其完成
            Process process = pb.start();
            process.waitFor();

            // 读取进程输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.lines().collect(Collectors.joining("\n"));

            // 关闭输入流
            reader.close();

            // 返回执行结果
            log.info("执行结果:"+ output);
            return output;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ""; // 或者抛出异常，根据需求处理
        }
    }

    public String runPythonScript(List<String> params) {
        try {
            // 构建命令行参数，包括 Python 解释器和脚本路径
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);

            // 将参数添加到命令行参数中
            pb.command().addAll(params);

            // 启动进程并等待其完成
            Process process = pb.start();
            process.waitFor();

            // 读取进程输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.lines().collect(Collectors.joining("\n"));

            // 关闭输入流
            reader.close();

            // 返回执行结果
            return output;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ""; // 或者抛出异常，根据需求处理
        }
    }

    public static void main(String[] args) {

        // python3 /opt/sqlite/script/main.py h3c switch 192.168.100.1 ssh 22 metoo metoo89745000 aliveint

        PythonScriptRunner runner = new PythonScriptRunner();

        String result = runner.runPythonScript("python3","/opt/sqlite/script/main.py",
                "h3c", "switch", "192.168.100.1",
                "ssh", "22", "metoo", "metoo89745000", "aliveint");

        System.out.println("Python script output:\n" + result);
    }


    @Test
    public void execTest(){
        String path = "/opt/netmap/os-scanner/os-scanner/";
        String commond = "cd /opt/netmap/os-scanner/os-scanner1/ && ./OS-scanner -i 192.168.6.1 -o 22 -c 1";
        this.exec(path, commond);
    }

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

            // 将参数添加到命令行参数中
            pb.command().addAll(Arrays.asList(args));

            // 启动进程并等待其完成
            Process process = pb.start();

            // 创建 StringBuilder 来收集输出
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

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

            // 创建线程读取标准错误流
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 启动线程
            outputThread.start();
            errorThread.start();

            // 等待进程完成
            try {
                int exitCode = process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // 等待线程完成
            try {
                // 等待线程完成
                outputThread.join();
                errorThread.join();

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

    public String exec_exe(String scriptPath, String scriptName, String... args) {
        try {
            // 构建命令行参数
            List<String> command = new ArrayList<>();
            command.add(scriptPath + File.separator + scriptName);
            command.addAll(Arrays.asList(args));

            // 初始化 ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(scriptPath));
            pb.redirectErrorStream(true); // 将错误流合并到标准输出流

            // 启动进程
            Process process = pb.start();

            StringBuffer output = new StringBuffer();// StringBuilder

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

            try {
                // 等待线程完成
                outputThread.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String cleanedOutput = output.toString().replaceAll("[\n\r]", "");
            return cleanedOutput;

        } catch (IOException e) {
            e.printStackTrace();
            return ""; // 或者根据需求抛出异常
        }
    }

    public static String execBinary(String binaryPath, String... args) {
        Process process = null;
        try {
            File binaryFile = new File(binaryPath);
            if (!binaryFile.exists()) {
                throw new FileNotFoundException("Binary file not found: " + binaryPath);
            }

            // 构建命令
            List<String> command = new ArrayList<>();
            command.add(binaryPath);
            command.addAll(Arrays.asList(args));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 合并错误流和输出流
            pb.directory(binaryFile.getParentFile()); // 设置工作目录为二进制文件所在目录

            process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Process exited with code: " + exitCode + "\nOutput: " + output);
            }

            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute binary: " + binaryPath, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}
