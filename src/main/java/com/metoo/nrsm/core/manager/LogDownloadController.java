package com.metoo.nrsm.core.manager;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/logs")
public class LogDownloadController {

    private static final Map<String, LogConfig> LOG_CONFIGS = new HashMap<>();
    static {
        LOG_CONFIGS.put("dhcp", new LogConfig(
                "/var/log/dhcp",
                Arrays.asList("dhcp.log-")
        ));
        LOG_CONFIGS.put("dhcp6", new LogConfig(
                "/var/log/dhcp6",
                Arrays.asList("dhcp6.log-")
        ));
        LOG_CONFIGS.put("dns", new LogConfig(
                "/var/log/unbound",
                Collections.singletonList("dns.log-")
        ));
        /*LOG_CONFIGS.put("dhcp", new LogConfig(
                "C:\\Users\\leo\\Desktop\\dhcp",
                Arrays.asList("dhcpd.log-")
        ));
        LOG_CONFIGS.put("dhcp6", new LogConfig(
                "C:\\Users\\leo\\Desktop\\dhcp6",
                Arrays.asList("dhcpd6.log-")
        ));
        LOG_CONFIGS.put("dns", new LogConfig(
                "C:\\Users\\leo\\Desktop\\unbound",
                Collections.singletonList("dns.log-")
        ));*/

    }

    @GetMapping("/download/{type}")
    public ResponseEntity<Resource> downloadLogs(
            @PathVariable String type,
            @RequestParam String start,
            @RequestParam(required = false) String end) throws IOException {

        LogConfig config = LOG_CONFIGS.get(type.toLowerCase());
        if (config == null) {
            return ResponseEntity.badRequest().body(null);
        }

        LocalDate startDate = parseDate(start);
        LocalDate endDate = (end == null) ? startDate : parseDate(end);
        List<Path> logFiles = collectLogFiles(config, startDate, endDate);

        return prepareResponse(logFiles);
    }

    private List<Path> collectLogFiles(LogConfig config, LocalDate start, LocalDate end) {
        List<Path> files = new ArrayList<>();
        LocalDate logToday = LocalDate.now().minusDays(1); // 关键调整：当天日志对应实际日期-1

        LocalDate current = start;
        while (!current.isAfter(end)) {
            String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);

            for (String prefix : config.filePrefixes) {
                // 判断是否为"当天"日志（实际日期-1）
                String extension = current.isEqual(logToday) ? "" : ".gz";
                String fileName = prefix + dateStr + extension;

                Path filePath = Paths.get(config.directory, fileName);
                if (Files.exists(filePath)) {
                    files.add(filePath);
                }
            }
            current = current.plusDays(1);
        }
        return files;
    }

    private ResponseEntity<Resource> prepareResponse(List<Path> files) throws IOException {
        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 单个文件直接返回原始格式（可能是.gz或未压缩）
        if (files.size() == 1) {
            Path file = files.get(0);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(file.toFile()));
        }

        // 多文件打包时保留原始格式
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Path file : files) {
                addToZip(zos, file);
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(bos.toByteArray()));
    }

    private void addToZip(ZipOutputStream zos, Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            ZipEntry entry = new ZipEntry(file.getFileName().toString());
            zos.putNextEntry(entry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }

    static class LogConfig {
        String directory;
        List<String> filePrefixes;

        LogConfig(String dir, List<String> prefixes) {
            this.directory = dir;
            this.filePrefixes = prefixes;
        }
    }
}