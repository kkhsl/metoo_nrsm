package com.metoo.nrsm.core.utils.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
@Service
public class RetryService {

    @Autowired
    private RestTemplate restTemplate;

    // 定义重试规则
    @Retryable(
            value = {ResourceAccessException.class },  // 捕获超时异常
            maxAttempts = 3,  // 最大重试次数
            backoff = @Backoff(delay = 2000, multiplier = 2)  // 延迟设置，每次延迟 2 秒
    )
    public void callExternalService() {
        String url = "http://127.0.0.1:8931/api/nrsm/traffic/data/{data}";

        // 发送请求，模拟一个可能超时的请求
//        try {
//            String response = restTemplate.getForObject(url, String.class, "data");
//            System.out.println("Response: " + response);
//        } catch (ResourceAccessException e) {
//            // 这里捕获异常，确保@Retryable可以触发重试
//            System.err.println("Caught ResourceAccessException: " + e.getMessage());
//            throw e;  // 重新抛出异常以触发重试
//        }


        String response = restTemplate.getForObject(url, String.class, "data");
        System.out.println("Response: " + response);
    }

    // 恢复方法：捕获最大重试次数达到后抛出的异常
    @Recover
    public void recover(SocketTimeoutException e) {
        System.err.println("Max retry attempts reached. Handling failure after retries: " + e.getMessage());
        // 处理重试失败的逻辑，如记录日志、发送通知等
    }

    // 如果发生 ResourceAccessException，进入这个恢复方法
    @Recover
    public void recover(ResourceAccessException e) {
        System.err.println("Resource access failed after retries: " + e.getMessage());
        // 处理其他 ResourceAccessException 错误的恢复
    }

}
