package com.metoo.nrsm.core.utils.unbound.strategy.impl;

import com.metoo.nrsm.core.utils.unbound.strategy.ConfigUpdateStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrivateAddressUpdateStrategy implements ConfigUpdateStrategy {

    @Override
    public List<String> updateConfig(List<String> lines, Object configData) throws IOException {
        Boolean privateAddress = (Boolean) configData;
        // 处理 local-zone 的更新逻辑
        List<String> updatedLines = updateLines(lines, privateAddress);
        return updatedLines;
    }

    public static List<String> updateLines(List<String> lines, boolean privateAddress) throws IOException {

        List<String> updatedLines = new ArrayList<>();

        // 遍历每一行
        for (String line : lines) {
            // 去掉行首尾的空格并去除注释符号（如果有）
            String trimmedLine = line.trim();
            String indentation = line.substring(0, line.indexOf(trimmedLine));
            System.out.println(trimmedLine.replaceFirst("^#\\s*", ""));

            // 判断该行是否包含 "private-address: ::/0"（无论是否有注释）
            if (trimmedLine.replaceFirst("^#\\s*", "").startsWith("private-address: ::/0")) {
                // 获取当前行的缩进部分
                String value = trimmedLine.replaceFirst("^#\\s*", "");
                if (privateAddress) {
                    // 如果 privateAddress 为 true 且该行是注释掉的，则去掉注释

                    String provateAddres = trimmedLine.startsWith("#") ? indentation + value : indentation + "# " + value;
                    updatedLines.add(provateAddres);
                } else {
                    // 如果 privateAddress 为 false，则注释掉该行
                    String provateAddres = trimmedLine.startsWith("#") ? indentation + value : indentation + "# " + value;
                    updatedLines.add(provateAddres);
                }
            }
            else {
                // 对于其他行，直接添加
                updatedLines.add(line);
            }
        }
        return updatedLines;
    }

}