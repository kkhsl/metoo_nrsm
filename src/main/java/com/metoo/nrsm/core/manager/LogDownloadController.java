package com.metoo.nrsm.core.manager;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/logs")
public class LogDownloadController {

    private static final Map<String, LogConfig> LOG_CONFIGS = new HashMap<>();
    static {
//        LOG_CONFIGS.put("dhcp", new LogConfig(
//                "/var/log/dhcp",
//                Arrays.asList("dhcpd.log-")
//        ));
//        LOG_CONFIGS.put("dhcp6", new LogConfig(
//                "/var/log/dhcp6",
//                Arrays.asList("dhcpd6.log-")
//        ));
//        LOG_CONFIGS.put("dns", new LogConfig(
//                "/var/log/unbound",
//                Collections.singletonList("dns.log-")
//        ));
        LOG_CONFIGS.put("dhcp", new LogConfig(
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
        ));

    }

    @GetMapping("/download/{type}")
    public ResponseEntity<?> downloadLogs(
            @PathVariable String type,
            @RequestParam String start,
            @RequestParam(required = false) String end) throws IOException {

        LogConfig config = LOG_CONFIGS.get(type.toLowerCase());
        if (config == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "config type error: " + type);
            return ResponseEntity.badRequest().body(error);
        }

        LocalDate startDate = parseDate(start);
        LocalDate endDate = (end == null) ? startDate : parseDate(end);
        List<Path> logFiles = collectLogFiles(config, startDate, endDate);

        return prepareResponse(logFiles);
    }

    private List<Path> collectLogFiles(LogConfig config, LocalDate start, LocalDate end) {
        List<Path> files = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
            for (String prefix : config.filePrefixes) {
                // 始终使用.gz扩展名
                String fileName = prefix + dateStr + ".gz";
                Path filePath = Paths.get(config.directory, fileName);
                if (Files.exists(filePath)) {
                    files.add(filePath);
                }
            }
            current = current.plusDays(1);
        }
        return files;
    }

    private ResponseEntity<?> prepareResponse(List<Path> files) throws IOException {
        if (files.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "log or directory is null");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
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


    @GetMapping("/dates/{type}")
    public ResponseEntity<Map<String, Object>> getAvailableDates(@PathVariable String type) {
        LogConfig config = LOG_CONFIGS.get(type.toLowerCase());
        if (config == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid log type: " + type);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Path dir = Paths.get(config.directory);
        if (!Files.isDirectory(dir)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Log directory not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // 使用有序集合自动排序
        Set<LocalDate> dateSet = new TreeSet<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path filePath : directoryStream) {
                String fileName = filePath.getFileName().toString();
                for (String prefix : config.filePrefixes) {
                    // 仅处理以.gz结尾的文件
                    if (fileName.startsWith(prefix) && fileName.endsWith(".gz")) {
                        // 移除前缀和.gz后缀
                        String datePart = fileName.substring(prefix.length(), fileName.length() - 3);
                        try {
                            LocalDate date = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
                            dateSet.add(date);
                        } catch (DateTimeParseException e) {
                            // 忽略无效格式
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        if (dateSet.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Log not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<String> sortedDates = dateSet.stream()
                .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Collections.singletonMap("dates", sortedDates));
    }
}