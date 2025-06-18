package com.metoo.nrsm.core.config.utils.gather.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProcessExecutorCFScanner {
    private static final long DEFAULT_TIMEOUT = 30; // 默认超时时间30秒
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 跨平台执行二进制文件
     * @param directory 二进制文件路径
     * @param args 执行参数
     * @return 执行结果
     */
    public static ExecutionResult executeBinary(String directory, String fileNname, String... args) {
        return executeBinaryWithTimeout(directory, fileNname, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, args);
    }


    /**
     * 带超时控制的跨平台执行
     * @param directory 二进制文件路径
     * @param timeout 超时时间
     * @param unit 时间单位
     * @param args 执行参数
     * @return 执行结果
     */
    public static ExecutionResult executeBinaryWithTimeout(String directory, String fileName, long timeout, TimeUnit unit, String... args) {
        Process process = null;
        try {
            // 构建完整的文件路径
            String binaryPath = directory + File.separator + fileName;
            File binaryFile = new File(binaryPath);

            // 打印调试信息（生产环境可移除）
            System.out.println("准备执行文件: " + binaryFile.getAbsolutePath());
            System.out.println("参数: " + Arrays.toString(args));

            // 验证文件
            if (!binaryFile.exists()) {
                throw new FileNotFoundException("可执行文件未找到: " + binaryPath);
            }
            if (!binaryFile.canExecute() && !binaryFile.setExecutable(true)) {
                throw new IOException("无法设置文件执行权限: " + binaryPath);
            }

            // 构建命令
            List<String> command = new ArrayList<>();
            if (isWindows()) {
                // Windows下使用完整路径
                command.add(binaryFile.getAbsolutePath());
            } else {
                // Linux/Mac下
                command.add("./" + fileName); // 使用相对路径需要加./
            }
            command.addAll(Arrays.asList(args));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 合并错误流和输出流
            pb.directory(binaryFile.getParentFile()); // 设置工作目录


            log.info("执行命令: {}", String.join(" ", pb.command()));

            // 启动进程
            process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程完成或超时
            boolean finished = process.waitFor(timeout, unit);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Process timed out after " + timeout + " " + unit.toString().toLowerCase());
            }

            int exitCode = process.exitValue();
            return new ExecutionResult(exitCode, output.toString().trim());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute binary: " + directory, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * 执行结果封装类
     */
    public static class ExecutionResult {
        private final int exitCode;
        private final String output;

        public ExecutionResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }

        @Override
        public String toString() {
            return "ExitCode: " + exitCode + "\nOutput:\n" + output;
        }
    }
}
