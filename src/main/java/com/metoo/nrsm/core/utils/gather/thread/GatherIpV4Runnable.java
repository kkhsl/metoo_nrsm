package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.impl.Ipv4ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.NetworkElement;
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
@Component
public class GatherIpV4Runnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherIpV4Runnable() {
    }

    public GatherIpV4Runnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherIpV4Runnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
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
//    @SneakyThrows
    @Override
    public void run() {
//        try {
//            Thread.sleep(25000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String path = Global.PYPATH + "getarp.py";
        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};
        String result = PythonExecUtils.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            try {
                List<Ipv4> ipv4s = JSONObject.parseArray(result, Ipv4.class);
                if(ipv4s != null && ipv4s.size()>0){
                    ipv4s.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
                    });
                    Ipv4ServiceImpl ipv4Service = (Ipv4ServiceImpl) ApplicationContextUtils.getBean("ipv4ServiceImpl");
                    ipv4Service.batchSaveGather(ipv4s);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
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
