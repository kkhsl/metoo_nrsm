package com.metoo.nrsm.core.utils.api;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-29 21:35
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置超时，避免调用创发api，避免阻塞程序
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)  // 设置连接超时为10秒
                .setConnectionRequestTimeout(5000)  // 设置请求超时为10秒
                .setSocketTimeout(5000)  // 设置读取超时为10秒
                .build();

        // 默认 HttpURLConnection
        // 创建带有超时设置的HttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
//                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();

        // 创建RestTemplate并设置自定义HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

}
