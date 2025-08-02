package com.metoo.nrsm.core.utils.api;


import com.metoo.nrsm.core.utils.api.netmap.NetmapPushRequestParams;
import com.metoo.nrsm.core.vo.ProbeRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-29 21:11
 */
@Slf4j
@Service
public class TrafficPushApiService {

    // 操作日志
//    private final static String URL = "http://175.6.37.154:10000/api/general/log/data/{data}";
    private final static String URL = "http://175.6.37.154:10000/api/nrsm/traffic/data/{data}";

    // 测试异常
//    private final static String URL = "http://127.0.0.1:8930/api/nrsm/traffic/data/{data}";


    private final RestTemplate restTemplate;

    @Autowired
    public TrafficPushApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    //    @Retryable(value = {ResourceAccessException.class, SocketTimeoutException.class, ConnectTimeoutException.class, TimeoutException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 2000))
    public void sendDataToMTO(String data) throws SocketTimeoutException, ConnectTimeoutException, TimeoutException {
        if (StringUtils.isEmpty(data)) {
            return;
        }
        try {
            String response = restTemplate.getForObject(URL, String.class, data);
            log.info("Traffic API response: " + response);
        }/*catch (HttpHostConnectException e) {// 会被spring restTemplate异常封装类拦截
            log.error("无法连接到目标主机: {}", e.getMessage());
        }*/ catch (ResourceAccessException e) {
            log.error("网络连接问题: {}", e.getMessage());
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.error("HTTP 错误响应: 状态码 - {}", e.getStatusCode());
            log.error("请求失败: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("请求失败: {}", e.getMessage());
        } catch (Exception e) {
            log.info("其他异常： {}", e.getMessage());
        }
    }

    public void callDataLogToSelf(String data) {
        if (StringUtils.isEmpty(data)) {
            return;
        }
        try {
            String response = restTemplate.getForObject(URL, String.class, data);
            log.info("Self api response " + response);
        } /*catch (HttpHostConnectException e) {// 会被spring restTemplate异常封装类拦截
            log.error("无法连接到目标主机: {}", e.getMessage());
        }*/ catch (ResourceAccessException e) {
            log.error("网络连接问题: {}", e.getMessage());
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.error("HTTP 错误响应: 状态码 - {}", e.getStatusCode());
            log.error("请求失败: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("请求失败: {}", e.getMessage());
        } catch (Exception e) {
            log.info("其他异常： {}", e.getMessage());
        }
    }


    public void general(String data) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://175.6.37.154:10000/api/general/log/data/{data}";
//        String url = "http://127.0.0.1:8930/api/general/log/data/{data}";

        String response = restTemplate.getForObject(url, String.class, data);

        log.info("General api ============================== " + response);
    }

    public String callThirdPartyApi(String apiUrl, ProbeRequestVO request) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();

        String nonce = "asdf";
        String timestamp = String.valueOf(System.currentTimeMillis());
        headers.set("nonce", nonce);
        headers.set("timestamp", timestamp);
        try {
            String signature = sha1(timestamp + nonce);
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置请求体参数
        HttpEntity<ProbeRequestVO> requestBody = new HttpEntity<>(request, headers);

        // 发起POST请求，并获取响应
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestBody,
                String.class
        );
        // 返回响应内容
        return response.getBody();
    }

    private AtomicInteger attemptCount = new AtomicInteger(1);

    @Retryable(
            value = {ResourceAccessException.class},  // 捕获超时异常
            maxAttempts = 3,  // 最大重试次数
            backoff = @Backoff(delay = 2000, multiplier = 2)  // 延迟设置，每次延迟 2 秒
    )
    public String callThirdPartyApiT(String apiUrl, JindustryUnitRequest jindustryUnitRequest) {
        log.info("Calling API, attempt number: {}", attemptCount.getAndIncrement() + jindustryUnitRequest.getData());

        HttpHeaders headers = new HttpHeaders();
        headers.set("nonce", jindustryUnitRequest.getNonce());
        headers.set("timestamp", jindustryUnitRequest.getTimestamp());

        try {
            String signature = sha1(jindustryUnitRequest.getTimestamp() + jindustryUnitRequest.getNonce());
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating signature: {}", e.getMessage());
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("charset", "UTF-8");

        HttpEntity<String> requestBody = new HttpEntity<>(headers);
        try {
            String param = EncrypUtils.encrypt(jindustryUnitRequest.getData());
            requestBody = new HttpEntity<>(param, headers);
        } catch (Exception e) {
            log.error("Error param: {}", e.getMessage());
        }

        // API请求代码
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );
            log.info("Api response: {}", response.getBody());
            return response.getBody();
        } catch (ResourceAccessException e) {
            // 捕获ResourceAccessException，并检查是否是连接超时引起的
            Throwable cause = e.getCause();
            if (cause instanceof ConnectTimeoutException) {
                log.error("Caught ConnectTimeoutException: {}", cause.getMessage());
                // 重新抛出ResourceAccessException以触发重试
                throw new ResourceAccessException("Connection timed out", (IOException) cause);
            } else {
                log.error("Caught ResourceAccessException: {}", cause.getMessage());
                throw e;  // 其他ResourceAccessException的情况
            }
        } catch (Exception e) {
            log.error("Other error occurred: {}", e.getMessage());
            throw e;  // 重新抛出其他异常
        }
    }

    @Recover
    public void recover(SocketTimeoutException e) {
        System.err.println("Max retry attempts reached. Handling failure after retries: " + e.getMessage());
        // 处理重试失败的逻辑，如记录日志、发送通知等
    }

    @Recover
    public void recover(ConnectTimeoutException e) {
        System.err.println("Max retry attempts reached. Handling failure after retries: " + e.getMessage());
        // 处理重试失败的逻辑，如记录日志、发送通知等
    }


    public String callThirdPartyApiT111(String apiUrl, JindustryUnitRequest jindustryUnitRequest) throws Exception {
        log.info("Calling API, attempt number: {}", attemptCount.getAndIncrement() + jindustryUnitRequest.getData());

        HttpHeaders headers = new HttpHeaders();
        headers.set("nonce", jindustryUnitRequest.getNonce());
        headers.set("timestamp", jindustryUnitRequest.getTimestamp());

        try {
            String signature = sha1(jindustryUnitRequest.getTimestamp() + jindustryUnitRequest.getNonce());
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating signature: {}", e.getMessage());
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("charset", "UTF-8");


        String param = EncrypUtils.encrypt(jindustryUnitRequest.getData());
        HttpEntity<String> requestBody = new HttpEntity<>(param, headers);
        try {
            log.info("Api exchange =================================");
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );
            log.info("Api response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            throw e;  // 如果遇到其他异常，重新抛出
        }
    }


    @Retryable(
            value = {ResourceAccessException.class},  // 捕获超时异常
            maxAttempts = 3,  // 最大重试次数
            backoff = @Backoff(delay = 2000, multiplier = 2)  // 延迟设置，每次延迟 2 秒
    )
    public String callThirdPartyApiTRetries(String apiUrl, JindustryUnitRequest jindustryUnitRequest) {
        log.info("Calling API, attempt number: {}", attemptCount.getAndIncrement() + jindustryUnitRequest.getData());

        HttpHeaders headers = new HttpHeaders();
        headers.set("nonce", jindustryUnitRequest.getNonce());
        headers.set("timestamp", jindustryUnitRequest.getTimestamp());

        try {
            String signature = sha1(jindustryUnitRequest.getTimestamp() + jindustryUnitRequest.getNonce());
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating signature: {}", e.getMessage());
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("charset", "UTF-8");

        HttpEntity<String> requestBody = new HttpEntity<>(headers);
        try {
            String param = EncrypUtils.encrypt(jindustryUnitRequest.getData());
            requestBody = new HttpEntity<>(param, headers);
        } catch (Exception e) {
            log.error("Error param: {}", e.getMessage());
        }

        // API请求代码
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );
            log.info("Api response: {}", response.getBody());
            return response.getBody();
        } catch (ResourceAccessException e) {
            // 捕获ResourceAccessException，并检查是否是连接超时引起的
            Throwable cause = e.getCause();
            if (cause instanceof ConnectTimeoutException) {
                log.error("Caught ConnectTimeoutException: {}", cause.getMessage());
                // 重新抛出ResourceAccessException以触发重试
                throw new ResourceAccessException("Connection timed out", (IOException) cause);
            } else {
                log.error("Caught ResourceAccessException: {}", cause.getMessage());
                throw e;  // 其他ResourceAccessException的情况
            }
        } catch (Exception e) {
            log.error("Other error occurred: {}", e.getMessage());
            throw e;  // 重新抛出其他异常
        }
    }


    // 超时
    public String callThirdPartyApiTT(String apiUrl, JindustryUnitRequest jindustryUnitRequest) throws Exception {

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();

        headers.set("nonce", jindustryUnitRequest.getNonce());
        headers.set("timestamp", jindustryUnitRequest.getTimestamp());
        try {
            String signature = sha1(jindustryUnitRequest.getTimestamp() + jindustryUnitRequest.getNonce());
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置字符集编码为 UTF-8(必须)
        headers.set("charset", "UTF-8");

        // 设置请求体参数 jindustryUnitRequest-进行国安加密(必须)

        String param = EncrypUtils.encrypt(jindustryUnitRequest.getData());

        HttpEntity<String> requestBody = new HttpEntity<>(param, headers);
        try {
            log.info("Api exchange =================================");
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );
            log.info("Api body=================================" + response.getBody());

            return response.getBody();
        } /*catch (HttpHostConnectException e) {// 会被spring restTemplate异常封装类拦截
            log.error("无法连接到目标主机: {}", e.getMessage());
        }*/ catch (ResourceAccessException e) {
            log.error("网络连接问题: {}", e.getMessage());
            return "网络连接问题："+e.getMessage();
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.error("HTTP 错误响应: 状态码 - {}", e.getStatusCode());
            log.error("请求失败: {}", e.getMessage());
            return "HTTP 错误响应："+e.getMessage();
        } catch (RestClientException e) {
            log.error("请求失败: {}", e.getMessage());
            return "请求失败："+e.getMessage();
        } catch (Exception e) {
            log.info("其他异常： {}", e.getMessage());
            return "其他异常："+e.getMessage();
        }
    }




    // 超时
    public String callNetmapApi(String apiUrl, NetmapPushRequestParams netmapPushRequestParams) throws Exception {

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();

        headers.set("nonce", netmapPushRequestParams.getNonce());
        headers.set("timestamp", netmapPushRequestParams.getTimestamp());
        try {
            String signature = sha1(netmapPushRequestParams.getTimestamp() + netmapPushRequestParams.getNonce());
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置字符集编码为 UTF-8(必须)
        headers.set("charset", "UTF-8");

        HttpEntity<String> requestBody = new HttpEntity<>(netmapPushRequestParams.getData(), headers);
        try {
            log.info("Api exchange =================================");
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );
            log.info("Api body=================================" + response.getBody());

            return response.getBody();
        } /*catch (HttpHostConnectException e) {// 会被spring restTemplate异常封装类拦截
            log.error("无法连接到目标主机: {}", e.getMessage());
        }*/ catch (ResourceAccessException e) {
            log.error("网络连接问题: {}", e.getMessage());
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.error("HTTP 错误响应: 状态码 - {}", e.getStatusCode());
            log.error("请求失败: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("请求失败: {}", e.getMessage());
        } catch (Exception e) {
            log.info("其他异常： {}", e.getMessage());
        }
        return "";
    }


    public static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        String str = encodeHex(digest);
//        return Hex.toHexString(digest);
        return str;

//        StringBuilder sb = new StringBuilder();
//        for (byte b : digest) {
//            sb.append(String.format("%02x", b));
//        }
//        return sb.toString();
    }

    private static final char[] HEX_CODE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encodeHex(byte[] bytes) {
        StringBuilder r = new StringBuilder(bytes.length * 2);
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            r.append(HEX_CODE[b >> 4 & 15]);
            r.append(HEX_CODE[b & 15]);
        }

        return r.toString();
    }
}
