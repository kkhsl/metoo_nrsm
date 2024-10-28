package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.impl.Ipv6ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Ipv6;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherIpV6Runnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherIpV6Runnable() {
    }

    public GatherIpV6Runnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherIpV6Runnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }



    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        String path = Global.PYPATH +  "getarpv6.py";
        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};
        String result = PythonExecUtils.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            try {
                List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                if(array.size()>0){
                    array.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
                    });
                }

                Ipv6ServiceImpl ipv6Service = (Ipv6ServiceImpl) ApplicationContextUtils.getBean("ipv6ServiceImpl");

                ipv6Service.batchSaveGather(array);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(latch != null){
                    latch.countDown();
                }
            }
        }else{
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
