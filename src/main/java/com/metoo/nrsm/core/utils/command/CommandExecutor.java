package com.metoo.nrsm.core.utils.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class CommandExecutor {

    /**
     * 执行命令并返回输出
     *
     * @param command 命令字符串
     * @return 命令输出
     * @throws IOException
     * @throws InterruptedException
     */
    public static String executeCommand(String command) throws IOException, InterruptedException {
        // 将命令分割成列表形式
        List<String> commandList = Arrays.asList(command.split(" "));

        // 创建 ProcessBuilder 实例
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);

        // 合并标准输出和错误输出流
        processBuilder.redirectErrorStream(true);

        // 启动进程
        Process process = processBuilder.start();

        // 获取进程的输出流
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // 等待进程执行完成并获取退出码
        int exitCode = process.waitFor();

        // 如果命令执行失败，可以抛出异常
        if (exitCode != 0) {
            throw new IOException("命令执行失败，退出代码：" + exitCode);
        }

        return output.toString();
    }
}
