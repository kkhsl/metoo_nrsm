package com.metoo.nrsm.core.system.service.utils;

import com.metoo.nrsm.core.system.service.model.ServiceInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务信息解析器
 * 解析systemctl命令的输出并转换为ServiceInfo对象
 */
public class ServiceInfoParser {
    // 匹配Active状态的正则表达式，如：Active: active (running)
    private static final Pattern ACTIVE_PATTERN =
            Pattern.compile("Active:\\s+(\\w+)\\s+\\((\\w+)\\)");

    // 匹配Loaded状态的正则表达式，如：Loaded: loaded (enabled)
    private static final Pattern LOADED_PATTERN =
            Pattern.compile("Loaded:\\s+(\\w+)");

    /**
     * 解析systemctl命令输出
     *
     * @param inputStream 命令输出流
     * @return 解析后的ServiceInfo对象
     * @throws IOException 当读取流失败时抛出
     */
    public ServiceInfo parse(InputStream inputStream) throws IOException {
        ServiceInfo serviceInfo = new ServiceInfo();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            // 解析服务名称（从●开头的行）
            if (line.startsWith("● ")) {
                serviceInfo.setName(line.substring(2).split("\\s+")[0]);
            }

            // 解析Loaded状态（是否开机自启）
            Matcher loadedMatcher = LOADED_PATTERN.matcher(line);
            if (loadedMatcher.find()) {
                serviceInfo.setEnabled("enabled".equals(loadedMatcher.group(1)));
            }

            // 解析Active状态（是否正在运行）
            Matcher activeMatcher = ACTIVE_PATTERN.matcher(line);
            if (activeMatcher.find()) {
                String status = activeMatcher.group(1);  // active/inactive
                String detail = activeMatcher.group(2);  // running/exited等

                serviceInfo.setStatus(status + " (" + detail + ")");
                serviceInfo.setActive("active".equals(status));
            }
        }

        return serviceInfo;
    }
}
