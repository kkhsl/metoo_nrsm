package com.metoo.nrsm.core.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 *     Description: 注册过滤器
 */

@Configuration
public class WebConfiguration {


    public static void main(String[] args) {
        final List<String> excludedPaths = Arrays.asList("/nrsm/admin/gather/*");
    }

}
