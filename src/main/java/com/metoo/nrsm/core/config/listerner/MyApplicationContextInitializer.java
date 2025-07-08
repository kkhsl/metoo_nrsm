package com.metoo.nrsm.core.config.listerner;

import com.metoo.nrsm.core.service.ILicenseService;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.annotation.WebListener;

@WebListener
public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ILicenseService licenseService = (ILicenseService) applicationContext.getBean("licenseServiceImpl");
        System.out.println(licenseService);
    }
}
