package com.metoo.nrsm.core.utils.gather.gathermac;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-23 10:39
 */
@Component
public class GatherSingleThreadingMacUtils {

    @Autowired
    private IMacService macService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private GatherMacUtils gatherMacUtils;

    // 单线程采集
    public void gatherMac(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if (networkElements.size() > 0) {
            this.macService.truncateTable();
            for (NetworkElement networkElement : networkElements) {

                String path = Global.PYPATH + "gethostname.py";
                String[] params1 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String hostname = PythonExecUtils.exec(path, params1);


                // mac表增加remote-device，remote-port
                try {
                    path = Global.PYPATH + "getlldp.py";
                    String[] params3 = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String getlldp = PythonExecUtils.exec(path, params3);

                    List<Map> lldps = JSONObject.parseArray(getlldp, Map.class);

                    this.gatherMacUtils.setRemoteDevice(networkElement, lldps, hostname, date);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                path = Global.PYPATH + "getmac.py";
                // String result = PythonExecUtils.exec(path);
                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = PythonExecUtils.exec(path, params);
                if (!"".equals(result)) {
                    try {
                        List<Mac> array = JSONObject.parseArray(result, Mac.class);
                        if (array.size() > 0) {
                            array.forEach(e -> {
                                if ("3".equals(e.getType())) {
                                    e.setDeviceIp(networkElement.getIp());
                                    e.setDeviceName(networkElement.getDeviceName());
                                    e.setAddTime(date);
                                    e.setHostname(hostname);
//                                    e.setTag("L");
                                    String patten = "^" + "00:00:5e";
                                    boolean flag = this.gatherMacUtils.parseLineBeginWith(e.getMac(), patten);
                                    if (flag) {
                                        e.setTag("LV");
                                    }
                                    this.macService.save(e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                path = Global.PYPATH + "getportmac.py";
                // String result = PythonExecUtils.exec(path);
                String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                result = PythonExecUtils.exec(path, params2);
                if (!"".equals(result)) {
                    try {
                        List<Mac> array = JSONObject.parseArray(result, Mac.class);
                        if (array.size() > 0) {
                            array.forEach(e -> {
                                if ("1".equals(e.getStatus())) {
                                    e.setAddTime(date);
                                    e.setDeviceIp(networkElement.getIp());
                                    e.setDeviceName(networkElement.getDeviceName());
                                    e.setTag("L");
                                    e.setHostname(hostname);
                                    String patten = "^" + "00:00:5e";
                                    boolean flag = this.gatherMacUtils.parseLineBeginWith(e.getMac(), patten);
                                    if (flag) {
                                        e.setTag("LV");
                                    }
                                    this.macService.save(e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }


}
