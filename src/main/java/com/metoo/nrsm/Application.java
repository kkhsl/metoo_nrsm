package com.metoo.nrsm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAspectJAutoProxy
@MapperScan({"com.metoo.nrsm.core.mapper", "com.metoo.nrsm.core.api.mapper"})
@EnableTransactionManagement
@ServletComponentScan(basePackages = {"com.metoo.nrsm"})
@EnableScheduling //  注解启用Spring的调度任务功能（启动类增加该注解，使项目启动后执行定时任务）
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        Long time = System.currentTimeMillis();
        SpringApplication.run(Application.class);
        System.out.println("===应用启动耗时：" + (System.currentTimeMillis() - time) + "===");
    }


}
