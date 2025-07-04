package com.metoo.nrsm.core.utils.task.schdule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-12 11:29
 */
@Slf4j
@Component
public class TestScheduled {

    public static String cron;

    @Value("${spring.profiles.active}")
    public void setEnv(String cron) {
        TestScheduled.cron = cron;
    }


}
