package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.impl.Ipv4ServiceImpl;
import com.metoo.nrsm.core.service.impl.Ipv6ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.entity.nspm.Ipv4;
import com.metoo.nrsm.entity.nspm.Ipv6;
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
public class GatherIpV6Runnable implements Runnable{

    @Autowired
    private Ipv4Service ipv4Service;

    private NetworkElement networkElement;

    private Date date;

    public GatherIpV6Runnable() {
    }

    public GatherIpV6Runnable(NetworkElement networkElement, Date date) {
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
        String path = Global.PYPATH +  "getarpv6.py";
//                result = PythonExecUtils.exec(path);
        String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};
        String result = PythonExecUtils.exec(path, params2);
        if(!"".equals(result)){
            try {
                List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                if(array.size()>0){
                    array.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
//                                this.ipv6Service.saveGather(e);
                    });
                }
                Ipv6ServiceImpl ipv6Service = (Ipv6ServiceImpl) ApplicationContextUtils.getBean("ipv6Service");
                ipv6Service.batchSaveGather(array);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
