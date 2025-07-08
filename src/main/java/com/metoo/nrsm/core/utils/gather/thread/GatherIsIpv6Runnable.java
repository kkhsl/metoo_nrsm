package com.metoo.nrsm.core.utils.gather.thread;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherIsIpv6Runnable implements Runnable {

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherIsIpv6Runnable() {
    }

    public GatherIsIpv6Runnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherIsIpv6Runnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }


    @Override
    public void run() {
        PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");
        String path = Global.PYPATH + "isipv6.py";
        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};
        String result = pythonExecUtils.exec(path, params);
        if (StringUtil.isNotEmpty(result)) {
            INetworkElementService networkElementServiceImpl = (INetworkElementService) ApplicationContextUtils.getBean("networkElementServiceImpl");
            try {
                networkElement.setIsipv6(Boolean.valueOf(result));
                networkElementServiceImpl.update(networkElement);
            } catch (Exception e) {
                e.printStackTrace();
                log.info(networkElement.getIp() + " : " + result);
            } finally {
                if (latch != null) {
                    latch.countDown();
                }
            }
        }
    }
}
