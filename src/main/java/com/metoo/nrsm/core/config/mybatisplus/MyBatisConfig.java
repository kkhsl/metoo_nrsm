package com.metoo.nrsm.core.config.mybatisplus;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 注册自定义 TypeHandler
            configuration.getTypeHandlerRegistry().register(ListStringToJsonTypeHandler.class);
        };
    }
}
