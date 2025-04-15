package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.utils.system.DiskInfo;
import com.metoo.nrsm.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@RequestMapping("/admin/test")
@RestController
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("getDHCP")
    public String getDHCP(){
        return SNMPv2Request.getDhcpStatus();
    }

    @GetMapping("getTraffic")
    public String getTraffic(){
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("113.240.243.196")
                .community("transfar@123")
                .port(161)
                .build();

        String traffic = SNMPv3Request.getTraffic(snmpv3Params, "1.3.6.1.2.1.31.1.1.1.6.31", "1.3.6.1.2.1.31.1.1.1.10.31");
        log.info("流量数据", traffic);
        return traffic;
    }
//
//    public static void main(String[] args) {
//        SystemInfo systemInfo = new SystemInfo();
//        CentralProcessor processor = systemInfo.getHardware().getProcessor();
//
//        // 获取 CPU 核心数量
//        int cpuCoreCount = processor.getLogicalProcessorCount();
//
//        // 获取 CPU 使用情况
//        long[] prevTicks = processor.getSystemCpuLoadTicks();
//        Util.sleep(1000);  // 等待 1 秒钟，以便获取负载变化
//        long[] ticks = processor.getSystemCpuLoadTicks();
//
//        // 计算每个核心的 CPU 使用率
//        double totalCpuLoad = 0.0;
//        for (int i = 0; i < cpuCoreCount; i++) {
//            // 获取每个核心的 CPU 使用率
//            double cpuLoad = processor.getProcessorCpuLoadBetweenTicks(prevTicks[i], ticks[i]) * 100;
//            System.out.println("CPU Core " + i + " usage: " + String.format("%.2f", cpuLoad) + "%");
//            totalCpuLoad += cpuLoad;
//        }
//
//        // 计算整体 CPU 使用率
//        double averageCpuLoad = totalCpuLoad / cpuCoreCount;
//        System.out.println("Total CPU usage: " + String.format("%.2f", averageCpuLoad) + "%");
//    }

    @GetMapping("cpuinfo")
    public void cpuinfo() {
        // 获取 CPU 数量
        int cpuCount = Runtime.getRuntime().availableProcessors();
        System.out.println("CPU 数量: " + cpuCount);

        // 获取操作系统的 CPU 使用率（如果支持）
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            try {
                double cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;  // 获取 CPU 使用率，乘以 100 转为百分比
                System.out.println("CPU 使用率: " + cpuLoad + "%");
            } catch (UnsupportedOperationException e) {
                System.out.println("当前 JVM 不支持获取 CPU 使用率");
            }
        } else {
            System.out.println("无法获取 CPU 使用率，当前 JVM 不支持该操作");
        }
    }


    public String metrics() {

        // 创建 SystemInfo 实例来获取系统信息
        SystemInfo systemInfo = new SystemInfo();

        // 获取硬件信息

        // 获取操作系统信息
        OperatingSystem os = systemInfo.getOperatingSystem();
        FileSystem fileSystem = os.getFileSystem();

        // 获取 CPU 使用情况
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);  // 等待 1 秒钟以便获取负载变化
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        // 获取内存信息
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalMemory = memory.getTotal();  // 总内存
        long availableMemory = memory.getAvailable();  // 可用内存
        double memoryUsage = 100.0 * (1 - ((double) availableMemory / (double) totalMemory));

        log.info("CPU 使用率: " + String.format("%.2f", cpuLoad) + "%");
        log.info("总内存: " + totalMemory / (1024 * 1024 * 1024) + " GB");
        log.info("可用内存: " + availableMemory / (1024 * 1024 * 1024) + " GB");
        log.info("内存使用率: " + String.format("%.2f", memoryUsage) + "%");


        return "";
    }



    @GetMapping("cpu")
    public void cpu() {
        // 创建 SystemInfo 对象，获取操作系统硬件信息
        SystemInfo systemInfo = new SystemInfo();
        // 获取 CPU 信息
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        // 获取所有核心的 CPU 使用率

        long[][] prevTicks = processor.getProcessorCpuLoadTicks();

        // Wait some time or run this in a loop to compare later
        // After some time, you can call it again to get the current ticks
        long[][] currentTicks = processor.getProcessorCpuLoadTicks();

        double[] cpuLoad = processor.getProcessorCpuLoadBetweenTicks(prevTicks);

        System.out.println("每个核心的 CPU 使用率:");
        for (int i = 0; i < cpuLoad.length; i++) {

            System.out.println("核心 " + i + ": " + cpuLoad[i] * 100 + "%");

            log.info("核心 " + i + ": " + cpuLoad[i] * 100 + "%");
        }


//        CentralProcessor processorAll = systemInfo.getHardware().getProcessor();
//
//        // 获取系统整体 CPU 使用率
//        double cpuUsage = processorAll.getSystemCpuLoad() * 100;
//
//        System.out.println("CPU 总使用率: " + cpuUsage + "%");

//        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        double cpuLoad = osBean.getSystemCpuLoad() * 100;
//        System.out.println("CPU Usage: " + cpuLoad + "%");

        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        // 获取系统的加载信息
        double systemLoad = bean.getSystemLoadAverage();
        double availableProcessors = bean.getAvailableProcessors();

        // 计算CPU使用率
        double cpuLoadAll = (systemLoad / availableProcessors) * 100;
        log.info("核心总使用率 " + cpuLoadAll + "%");
    }


    @GetMapping("mem")
    public void mem() {

        // 创建 SystemInfo 实例来获取系统信息
        SystemInfo systemInfo = new SystemInfo();

        GlobalMemory memory = systemInfo.getHardware().getMemory();

        // 获取内存信息
        long totalMemory = memory.getTotal();  // 总内存
        long availableMemory = memory.getAvailable();  // 可用内存
        double memoryUsage = 100.0 * (1 - ((double) availableMemory / (double) totalMemory));
        log.info("内存使用率: " + String.format("%.2f", memoryUsage) + "%");
    }

    @GetMapping("disk")
    public void disk(){
        DiskInfo.getDiskSpaceInformation();

        DiskInfo.getRootDiskSpaceInformation();


//        FileSystems.getDefault().getFileSystem().getRootDirectories().forEach(path -> {
//            try {
//                FileSystem fs = path.getFileSystem();
//                System.out.println("Disk: " + path);
//                System.out.println("Total Space: " + fs.getPath(path.toString()).toFile().getTotalSpace());
//                System.out.println("Usable Space: " + fs.getPath(path.toString()).toFile().getUsableSpace());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }


    @GetMapping("api")
    public void testApi(){
        // 假设你有一个请求对象
        User requestObject = new User();
        requestObject.setEmail("value1");

        // 调用 API
        String apiUrl = "http://127.0.0.1:8960/nrsm/admin/test/api2";
        String response = callApi(apiUrl, requestObject);

        // 处理响应
        System.out.println("API Response: " + response);

    }

    @PostMapping("api2")
    public void testApi2() throws InterruptedException {
        Thread.sleep(300000);
        log.info("api_back 2");
    }

    public String callApi(String apiUrl, Object requestBody) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer your-token"); // 如果需要设置token

            // 将请求体封装到 HttpEntity 中
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

            // 调用 API，发送 POST 请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,                    // API URL
                    HttpMethod.POST,           // 请求方法
                    entity,                    // 请求体和头部
                    String.class               // 返回类型
            );

            // 返回响应体
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            // 错误处理
            return "Error: " + e.getMessage();
        }
    }


    // 周末和休息时间（17:周末和休息时间（17:30-8:30）按上述范围的1/10取值30-8:30）按上述范围的1/10取值
    public String getlow(double min, double max) {

        Random random = new Random();

        // Generate a random double between min (inclusive) and max (exclusive)
        double randomValue = min + (max - min) * random.nextDouble();

        // Divide randomValue by ten
        double result = randomValue;

        System.out.println("isWeekend()" + isWeekend());

        System.out.println("timeCheck()" + timeCheck());

        if(isWeekend() || timeCheck()){
            result = result / 10.0;
        }

        // Format result to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedResult = df.format(result);
        return formattedResult;
    }

    // 封装判断是否为周末的方法
    public static boolean isWeekend() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    // 17:30-8:30

    @Test
    public void isInTime() {
//        LocalTime currentTime = LocalTime.now();
        LocalTime currentTime = LocalTime.of(8, 30);
        LocalTime endTime = LocalTime.of(17, 30);

        if (isAfter1730(currentTime) || isBefore830(currentTime)) {
            System.out.println("当前时间在17:30之后或8:30之前");
        } else {
            System.out.println("当前时间不在指定范围内");
        }
    }

    public boolean timeCheck(){
        LocalTime currentTime = LocalTime.now();
        if (isAfter1730(currentTime) || isBefore830(currentTime)) {
            System.out.println("当前时间在17:30之后或8:30之前");
            return true;
        } else {
            System.out.println("当前时间不在指定范围内");
            return false;
        }
    }

    public static boolean isAfter1730(LocalTime currentTime) {
        LocalTime endTime = LocalTime.of(17, 30);
        return currentTime.isAfter(endTime);
    }

    public static boolean isBefore830(LocalTime currentTime) {
        LocalTime startTime = LocalTime.of(8, 30);
        return currentTime.isBefore(startTime);
    }
}


