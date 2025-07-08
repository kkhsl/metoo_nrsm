package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.impl.MacServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherMacGetlldpSNMPRunnable implements Runnable {

    private NetworkElement networkElement;

    private Date date;

    private String hostname;

    private CountDownLatch latch;

    public GatherMacGetlldpSNMPRunnable() {
    }

    public GatherMacGetlldpSNMPRunnable(NetworkElement networkElement, Date date, String hostname, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.hostname = hostname;
        this.latch = latch;
    }


    @Override
    public void run() {
    }


}
