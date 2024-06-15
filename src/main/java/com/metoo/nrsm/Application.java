package com.metoo.nrsm;

import com.metoo.nrsm.core.config.redis.MyRedisManager;
import org.apache.logging.log4j.core.config.Order;
import org.junit.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@MapperScan("com.metoo.nrsm.core.mapper")
@EnableTransactionManagement
@ServletComponentScan(basePackages ={ "com.metoo.nrsm"})
@EnableScheduling //  注解启用Spring的调度任务功能（启动类增加该注解，使项目启动后执行定时任务）
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        Long time=System.currentTimeMillis();
        SpringApplication.run(Application.class);
        System.out.println("===应用启动耗时："+(System.currentTimeMillis()-time)+"===");
    }

}
