package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.impl.PortServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Port;
import com.metoo.nrsm.entity.NetworkElement;
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
public class GatherPortRunnable implements Runnable{


    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherPortRunnable() {
    }

    public GatherPortRunnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherPortRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
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
        try {

            PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

            String path = Global.PYPATH + "getporttable.py";
            String[] params = {networkElement.getIp(), networkElement.getVersion(),
                    networkElement.getCommunity()};
            String result = pythonExecUtils.exec2(path, params);
            if(StringUtil.isNotEmpty(result)){
                List<Port> ports = JSONObject.parseArray(result, Port.class);
                if(ports.size()>0){
                    ports.forEach(e -> {
                        e.setDeviceUuid(networkElement.getUuid());
                        e.setAddTime(date);
                    });
                    PortServiceImpl portService = (PortServiceImpl) ApplicationContextUtils.getBean("portServiceImpl");
                    portService.batchSaveGather(ports);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
