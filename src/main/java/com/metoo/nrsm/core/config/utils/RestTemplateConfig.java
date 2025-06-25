package com.metoo.nrsm.core.config.utils;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration("sseRestTemplateConfig")
public class RestTemplateConfig {


    @Bean("sseRestTemplate")
    public RestTemplate sseRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);       // 5秒连接超时
        factory.setReadTimeout(0);              // 0表示无读取超时（长轮询）

        // 创建UTF-8专用的RestTemplate
        RestTemplate restTemplate = new RestTemplateBuilder()
                .requestFactory(() -> factory)
                .build();

        // 配置消息转换器以支持UTF-8
        configureMessageConverters(restTemplate);

        return restTemplate;
    }

    private void configureMessageConverters(RestTemplate restTemplate) {
        // 获取当前的消息转换器列表
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

        // 创建UTF-8字符串转换器
        StringHttpMessageConverter utf8StringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        utf8StringConverter.setWriteAcceptCharset(false); // 防止添加额外字符集

        // 添加字节数组转换器
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();

        // 替换或添加转换器
        boolean hasStringConverter = false;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                // 替换默认的String转换器为UTF-8版本
                converters.set(i, utf8StringConverter);
                hasStringConverter = true;
            }
        }

        // 如果没有找到String转换器，添加新的
        if (!hasStringConverter) {
            converters.add(utf8StringConverter);
        }

        // 确保字节数组转换器存在
        if (!converters.stream().anyMatch(c -> c instanceof ByteArrayHttpMessageConverter)) {
            converters.add(byteArrayConverter);
        }

        // 设置新的转换器列表
        restTemplate.setMessageConverters(converters);
    }
}