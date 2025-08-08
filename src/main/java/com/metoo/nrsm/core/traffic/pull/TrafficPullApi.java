package com.metoo.nrsm.core.traffic.pull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class TrafficPullApi {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    /**
     * 从 application.properties 注入配置
     * 配置示例: traffic.api.base-url=http://192.168.7.102:50000
     */
    public TrafficPullApi(
            RestTemplate restTemplate,
            @Value("${traffic.api.base-url}") String baseUrl) {

        this.restTemplate = restTemplate;
        this.baseUrl = validateAndNormalizeBaseUrl(baseUrl);
    }


    // 校验并规范化BaseUrl
    private String validateAndNormalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("traffic.api.base-url 不能为空");
        }

        // 去除末尾的斜杠
        return baseUrl.endsWith("/") ?
                baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }


    /**
     * 查询网络流量数据
     * @param department 部门名称(如"综合部")
     * @param startTime 开始时间(格式: "yyyy-MM-dd HH:mm:ss")
     * @param endTime 结束时间(格式: "yyyy-MM-dd HH:mm:ss")
     * @param additionalParams 额外查询参数
     * @return API响应结果
     */
    public ApiResponse queryNetFlow(String department,
                                    String startTime,
                                    String endTime) {
        // 验证参数
        if (!isValidTimeFormat(startTime)) {
            return ApiResponse.error("开始时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
        if (!isValidTimeFormat(endTime)) {
            return ApiResponse.error("结束时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
        if (department == null || department.trim().isEmpty()) {
            return ApiResponse.error("部门名称不能为空");
        }

        try {
            // 构建查询参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("name", department);
            params.add("startTime", startTime);
            params.add("endTime", endTime);

            // 2. 使用 UriComponentsBuilder 构建完整 URL（自动编码）
            String fullUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParams(params)  // 自动添加查询参数
                    .build()
                    .toUriString();     // 生成完整 URL


            log.info("调用NetFlow API: {}, 参数: {}", fullUrl, params);

            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    fullUrl ,
                    HttpMethod.GET,
                    null,
                    Map.class,
                    params);

            // 处理响应
            if (response.getStatusCode().is2xxSuccessful()) {
                return ApiResponse.success(response.getBody());
            } else {
                String errorMsg = String.format("API请求失败，状态码: %d",
                        response.getStatusCodeValue());
                log.error(errorMsg);
                return ApiResponse.error(errorMsg);
            }
        } catch (RestClientException e) {
            log.error("API调用异常", e);
            return ApiResponse.error("API调用异常: " + e.getMessage());
        }
    }

    /**
     * 查询网络流量数据（GET请求，三个参数）
     * @param department 部门名称（中文）
     * @param startTime 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return API响应结果
     */
//    public ApiResponse queryNetFlow(String department, String startTime, String endTime) {
//        // 参数验证
//        if (department == null || department.trim().isEmpty()) {
//            return ApiResponse.error("部门名称不能为空");
//        }
//        if (!isValidTimeFormat(startTime)) {
//            return ApiResponse.error("开始时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
//        }
//        if (!isValidTimeFormat(endTime)) {
//            return ApiResponse.error("结束时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
//        }
//
//        try {
//            // 1. 构建查询参数（无需手动编码，UriComponentsBuilder会自动处理）
//            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//            params.add("name", department);    // 中文参数
//            params.add("startTime", startTime);
//            params.add("endTime", endTime);
//
//            // 2. 构建完整URL（自动编码中文参数）
//            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/netflow/flow/query")
//                    .queryParams(params)
//                    .build()
//                    .toUriString();
//
//            log.info("调用NetFlow API: {}", url);
//
//            // 3. 发送GET请求
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    null,  // GET请求通常不需要请求体
//                    Map.class);
//
//            // 4. 处理响应
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return ApiResponse.success(response.getBody());
//            } else {
//                String errorMsg = String.format("API请求失败，状态码: %d", response.getStatusCodeValue());
//                log.error(errorMsg);
//                return ApiResponse.error(errorMsg);
//            }
//        } catch (RestClientException e) {
//            log.error("API调用异常", e);
//            return ApiResponse.error("API调用异常: " + e.getMessage());
//        }
//    }

    // 验证时间格式
    private boolean isValidTimeFormat(String timeStr) {
        try {
            LocalDateTime.parse(timeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * API响应封装类
     */
    public static class ApiResponse {
        private final boolean success;
        private final Map<String, Object> data;
        private final String error;

        private ApiResponse(boolean success, Map<String, Object> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public static ApiResponse success(Map<String, Object> data) {
            return new ApiResponse(true, data, null);
        }

        public static ApiResponse error(String error) {
            return new ApiResponse(false, null, error);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public Map<String, Object> getData() { return data; }
        public String getError() { return error; }

        @Override
        public String toString() {
            return success ? "Success: " + data : "Error: " + error;
        }
    }
}
