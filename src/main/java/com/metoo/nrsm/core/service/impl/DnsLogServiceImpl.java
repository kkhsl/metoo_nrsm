package com.metoo.nrsm.core.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.metoo.nrsm.core.mapper.DnsTempLogMapper;
import com.metoo.nrsm.core.mapper.VendorMapper;
import com.metoo.nrsm.entity.DnsTempLog;
import com.metoo.nrsm.core.service.IDnsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

/**
 * dns解析服务
 *
 * @author zzy
 * @version 1.0
 * @date 2025/4/25 15:58
 */
@Service
@Slf4j
public class DnsLogServiceImpl implements IDnsLogService {
    private static final int THREAD_COUNT = 10;
    private static final int CHUNK_SIZE_MB = 100;
    private static final Pattern answerPattern = Pattern.compile(".*query\\s+response\\s+was\\s+ANSWER.*");
    private static final Pattern dataPattern = Pattern.compile("(\\d{10}).*? (\\d+\\.\\d+\\.\\d+\\.\\d+) (\\S+?) (A|AAAA) IN NOERROR");
    private static final Pattern cachePattern = Pattern.compile("(\\d{10}).*? (\\d+\\.\\d+\\.\\d+\\.\\d+) (\\S+?) (A|AAAA) IN");

    @Value("${logging.file.name}")
    private String logFilePath;
    @Resource
    private DnsTempLogMapper dnsTempLogMapper;

    /**
     * 解析日志数据
     */
    @Override
    public void parseLargeLog() {
        try {
            String curTime=DateUtil.offsetDay(DateUtil.date(),-1).toDateStr();
            List<MappedByteBuffer> chunks = splitFile(logFilePath+StrUtil.DASHED+curTime);
            ExecutorService executor = ThreadUtil.newExecutor(THREAD_COUNT);
            List<CompletableFuture<Void>> futures = chunks.stream()
                    .map(chunk -> CompletableFuture.runAsync(() ->
                    {
                        try {
                            processChunk(chunk);
                        } catch (Exception e) {
                            log.error("processChunk解析日志文件出错：{}", e);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
        } catch (Exception e) {
            log.error("parseLargeLog解析日志文件出错：{}", e);
        }
    }

    /**
     * 拆分文件
     *
     * @param logFilePath
     * @return
     * @throws IOException
     */
    private List<MappedByteBuffer> splitFile(String logFilePath) throws IOException {
        List<MappedByteBuffer> chunks = new ArrayList<>();
        try (FileChannel channel = FileChannel.open(Paths.get(logFilePath), READ)) {
            long fileSize = channel.size();
            long chunkSize = CHUNK_SIZE_MB * 1024L * 1024L;
            for (long pos = 0; pos < fileSize; pos += chunkSize) {
                long size = Math.min(chunkSize, fileSize - pos);
                chunks.add(channel.map(READ_ONLY, pos, size));
            }
        }
        return chunks;
    }

    /**
     * 具体解析逻辑
     *
     * @param buffer
     * @throws IOException
     */
    public void processChunk(MappedByteBuffer buffer) {
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
        int lineStart = 0;
        for (int i = 0; i < charBuffer.limit(); i++) {
            if (charBuffer.get(i) == '\n') {
                String currentLine = charBuffer.subSequence(lineStart, i).toString();
                // 非cache匹配
                Matcher matcher = answerPattern.matcher(currentLine);
                // cache匹配
                Matcher cacheMatcher = cachePattern.matcher(currentLine);
                if (matcher.find()) {
                    // 非缓存命中提取并处理下一行
                    if (i + 1 < charBuffer.limit()) {
                        int nextLineStart = i + 1;
                        int nextLineEnd = findNextLineEnd(nextLineStart, charBuffer);
                        String nextLine = charBuffer.subSequence(nextLineStart, nextLineEnd).toString();
                        processNextLine(nextLine,0);
                    }
                }else if(cacheMatcher.find()){
                    // 缓存命中，提取并处理下一行
                    if (i + 1 < charBuffer.limit()) {
                        int nextLineStart = i + 1;
                        int nextLineEnd = findNextLineEnd(nextLineStart, charBuffer);
                        String nextLine = charBuffer.subSequence(nextLineStart, nextLineEnd).toString();
                        processNextLine(nextLine,1);
                    }
                }
                lineStart = i + 1;
            }
        }
    }

    /**
     * 处理answer下一行数据
     *
     * @param dataLine
     */
    private void processNextLine(String dataLine,Integer isCache) {
        try {
            Matcher dataMatcher = dataPattern.matcher(dataLine);
            if (dataMatcher.find()) {
                // 提取关键信息
                String time = dataMatcher.group(1);
                String ip = dataMatcher.group(2);
                String domain = dataMatcher.group(3);
                String type = dataMatcher.group(4);
                // 处理域名结尾的.
                if (domain.endsWith(".")) {
                    domain = domain.substring(0, domain.length() - 1);
                }
                // 格式化输出
                String formattedDate = "";
                if (StrUtil.isNotEmpty(time)) {
                    Date date = new Date(Long.parseLong(time) * 1000);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    formattedDate = formatter.format(date);
                }
                dnsTempLogMapper.saveInfo(DnsTempLog.builder().logTime(DateUtil.parseDateTime(formattedDate))
                        .ip(ip).domainData(domain).type(type).isCache(isCache).build());
            }
        } catch (Exception e) {
            log.error("具体解析日志文件出错：{}", e);
        }
    }

    private int findNextLineEnd(int start, CharBuffer buffer) {
        for (int i = start; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                return i;
            }
        }
        return buffer.limit();
    }

    @Override
    public List<DnsTempLog> queryDnsLog() {
        return dnsTempLogMapper.queryRecordInfo();
    }

    @Override
    public void truncateTable() {
        dnsTempLogMapper.truncateTable();
    }

    @Override
    public void deleteDnsFile() {
        String curTime=DateUtil.offsetDay(DateUtil.date(),-1).toDateStr();
        String filePath=logFilePath+StrUtil.DASHED+curTime;
        File file=new File(filePath);
        if(file.delete()){
            log.info("删除dns日志文件成功:{}",filePath);
        }else{
            log.error("删除dns日志文件失败:{}",filePath);
        }
    }
}
