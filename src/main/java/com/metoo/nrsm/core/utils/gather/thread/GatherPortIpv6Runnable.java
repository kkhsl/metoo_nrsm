package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.impl.PortIpv6ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.PortIpv6;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Component
public class GatherPortIpv6Runnable implements Runnable {


    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherPortIpv6Runnable() {
    }

    public GatherPortIpv6Runnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherPortIpv6Runnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }


    @Override
    public void run() {
        try {
            PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

            String path = Global.PYPATH + "getporttableipv6.py";
            String[] params = {networkElement.getIp(), networkElement.getVersion(),
                    networkElement.getCommunity()};
            String result = pythonExecUtils.exec2(path, params);
            if (StringUtil.isNotEmpty(result)) {
                List<PortIpv6> ports = JSONObject.parseArray(result, PortIpv6.class);
                if (ports.size() > 0) {
                    ports.forEach(e -> {
                        e.setDeviceUuid(networkElement.getUuid());
                        e.setAddTime(date);
                    });
                    PortIpv6ServiceImpl portService = (PortIpv6ServiceImpl) ApplicationContextUtils.getBean("portIpv6ServiceImpl");
                    portService.batchSaveGather(ports);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
