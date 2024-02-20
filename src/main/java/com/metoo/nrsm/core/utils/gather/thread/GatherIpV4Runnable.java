package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.impl.Ipv4ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.entity.nspm.Ipv4;
import com.metoo.nrsm.entity.nspm.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Component
public class GatherIpV4Runnable implements Runnable{

    @Autowired
    private Ipv4Service ipv4Service;

    private NetworkElement networkElement;

    private Date date;

    public GatherIpV4Runnable() {
    }

    public GatherIpV4Runnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
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
        String path = Global.PYPATH + "getarp.py";
//                String result = PythonExecUtils.exec(path);
        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};
        String result = PythonExecUtils.exec(path, params);
        if(!"".equals(result)){
            try {
                List<Ipv4> ipv4s = JSONObject.parseArray(result, Ipv4.class);
                if(ipv4s.size()>0){
                    ipv4s.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
//                                this.ipv4Service.saveGather(e);
                    });
                    Ipv4ServiceImpl ipv4Service = (Ipv4ServiceImpl) ApplicationContextUtils.getBean("ipv4ServiceImpl");
                    ipv4Service.batchSaveGather(ipv4s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
