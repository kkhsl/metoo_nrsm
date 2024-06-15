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
public class GatherMacGetlldpRunnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private String hostname;

    private CountDownLatch latch;

    public GatherMacGetlldpRunnable() {
    }

    public GatherMacGetlldpRunnable(NetworkElement networkElement, Date date, String hostname, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.hostname = hostname;
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

        log.info(Thread.currentThread().getName() + ": getlldp.py + : " + networkElement.getIp());

        try {
            PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");
            String path = Global.PYPATH + "getlldp.py";
            String[] params = {networkElement.getIp(), networkElement.getVersion(),
                    networkElement.getCommunity()};
            String result = pythonExecUtils.exec2(path, params);
            if(StringUtil.isNotEmpty(result)){
                List<Map> lldps = JSONObject.parseArray(result, Map.class);
                this.setRemoteDevice(networkElement, lldps, hostname, date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }

    public void setRemoteDevice(NetworkElement e, List<Map> lldps, String hostname, Date date){
        // 写入对端信息
        if(lldps != null && lldps.size() > 0){

            MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
            List<Mac> list = new ArrayList();
            for(Map<String, String> obj : lldps){
                Mac mac = new Mac();
                mac.setAddTime(date);
                mac.setDeviceIp(e.getIp());
                mac.setDeviceName(e.getDeviceName());
//                mac.setPort(e.getPort());
                mac.setMac("00:00:00:00:00:00");
                mac.setHostname(hostname);
                mac.setTag("DE");
                mac.setRemotePort(obj.get("remoteport"));
                mac.setRemoteDevice(obj.get("hostname"));
//                macService.save(mac);
                list.add(mac);
            }
            if(list.size() > 0){
                macService.batchSaveGather(list);
            }
        }
    }

}
