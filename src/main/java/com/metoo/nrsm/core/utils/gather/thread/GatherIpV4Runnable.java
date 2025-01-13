package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.impl.Ipv4ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
//@Data
//@Component
public class GatherIpV4Runnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;


    public GatherIpV4Runnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }

    @Override
    public void run() {

        PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");
        Ipv4Service ipv4Service = (Ipv4Service) ApplicationContextUtils.getBean("ipv4ServiceImpl");

        String path = Global.PYPATH + "getarp.py";
        String[] params = {networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity()};
        try {
            String result = pythonExecUtils.exec(path, params);
            if(StringUtil.isNotEmpty(result)) {
                List<Ipv4> ipv4s = JSONObject.parseArray(result, Ipv4.class);
                if (ipv4s != null && ipv4s.size() > 0) {
                    ipv4s.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
                    });
                    ipv4Service.batchSaveGather(ipv4s);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
