package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.Ipv6Service;
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.Ipv6;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@RequestMapping("/admin/arp")
@RestController
public class ArpManagerController {

    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private Ipv6Service ipv6Service;
    @Autowired
    private IArpService arpService;
    @Autowired
    private INetworkElementService networkElementService;

    @GetMapping("getArp")
    public void ipv4(){
//        String path = "/opt/nrsm/py/getmac.py";
//        String[] params = {"v2c", "public@123"};
//        String result = PythonExecUtils.exec(path, params);
        String path = "E:\\python\\project\\djangoProject\\app01\\nrsm\\getarp.py";
        String result = PythonExecUtils.exec(path);
        if(!"".equals(result)){
            try {
                List<Ipv4> array = JSONObject.parseArray(result, Ipv4.class);
                if(array.size()>0){
                    this.ipv4Service.truncateTable();
                    array.forEach(e -> {
                        this.ipv4Service.save(e);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("ipv6")
    public void ipv6(){
//        String path = "/opt/nrsm/py/getmac.py";
//        String[] params = {"v2c", "public@123"};
//        String result = PythonExecUtils.exec(path, params);
        String path = "E:\\python\\project\\djangoProject\\app01\\nrsm\\getarpv6.py";
        String result = PythonExecUtils.exec(path);
        if(!"".equals(result)){
            try {
                List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                if(array.size()>0){
                    this.ipv6Service.truncateTable();
                    array.forEach(e -> {
                        this.ipv6Service.save(e);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @GetMapping("arp")
    public void arp(){
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0){

            this.ipv4Service.truncateTable();
            this.ipv6Service.truncateTable();

            for (NetworkElement networkElement : networkElements) {
                String path = "E:\\python\\project\\djangoProject\\app01\\nrsm\\getarp.py";
                String result = PythonExecUtils.exec(path);
                if(!"".equals(result)){
                    try {
                        List<Ipv4> array = JSONObject.parseArray(result, Ipv4.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                this.ipv4Service.save(e);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                path = "E:\\python\\project\\djangoProject\\app01\\nrsm\\getarpv6.py";
                result = PythonExecUtils.exec(path);
                if(!"".equals(result)){
                    try {
                        List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                this.ipv6Service.save(e);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.arpService.truncateTable();
        this.arpService.writeArp();
    }

    @GetMapping("exec")
    public void exec(){
       this.arpService.gatherArp(new Date());
    }
}
