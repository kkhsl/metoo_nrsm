package com.metoo.nrsm.core.utils.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.dto.GatherJsonDto;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * json文件读取
 * @author zhaozhiyuan
 * @version 1.0
 * @date 2024/10/5 21:10
 */
@UtilityClass
public class JsonFileToDto {
    /**
     * 读取json文件
     * @param filePath
     * @return
     * @throws IOException
     */
    public static GatherJsonDto readDataFromJsonFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        return objectMapper.readValue(file, GatherJsonDto.class);
    }
    /**
     * 将一个列表分成指定数量的子列表
     *
     * @param originalList 原始列表
     * @param numSubLists  要分成的子列表数量
     * @return 分割后的子列表集合
     */
    public static <T> List<List<T>> splitList(List<T> originalList, int numSubLists) {
        List<List<T>> result = new ArrayList<>();
        int size = originalList.size();
        int quotient = size / numSubLists; // 每个子列表的平均大小
        int remainder = size % numSubLists; // 剩余的元素数量

        for (int i = 0; i < numSubLists; i++) {
            int start = i * quotient + Math.min(i, remainder);
            int end = (i + 1) * quotient + Math.min(i + 1, remainder);
            List<T> subList = originalList.subList(start, end);
            result.add(new ArrayList<>(subList)); // 使用新的 ArrayList 包装子列表
        }
        return result;
    }
}
