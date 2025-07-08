package com.metoo.nrsm.core.config.shiro;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * Description: Swagger控制器; Swagger首页：
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {


    @Bean
    public Docket docket1(Environment environment) {
        Profiles profiles = Profiles.of("dev");
        boolean flag = environment.acceptsProfiles(profiles);

        // 获取项目环境
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("swagger");
    }

    @Bean
    public Docket docket(Environment environment) {
        Profiles profiles = Profiles.of("dev");
        boolean flag = environment.acceptsProfiles(profiles);

        // 获取项目环境
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("默认文档")
                .apiInfo(apiInfo())
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.ant("/admin/**"))
                .build();
    }

    public ApiInfo apiInfo() {
        Contact contact = new Contact("HKK", "httpclient://www.apache.org/licenses/LICENSE-2.0", "460751446@qq.com");
        return new ApiInfo(
                "Metoo Api Decument"
                , "屎壳郎推地球！"
                , "1.0"
                , "urn:tos"
                , contact
                , "Apache 2.0"
                , "httpclient://www.apache.org/licenses/LICENSE-2.0"
                , new ArrayList());
    }

}
