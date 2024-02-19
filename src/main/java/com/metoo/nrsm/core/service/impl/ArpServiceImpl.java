package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.mapper.ArpMapper;
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.Ipv6Service;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.entity.nspm.Arp;
import com.metoo.nrsm.entity.nspm.Ipv4;
import com.metoo.nrsm.entity.nspm.Ipv6;
import com.metoo.nrsm.entity.nspm.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:20
 */
@Service
@Transactional
public class ArpServiceImpl implements IArpService {

    @Autowired
    private ArpMapper arpMapper;
    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private Ipv6Service ipv6Service;
    @Autowired
    private INetworkElementService networkElementService;

    @Override
    public List<Arp> selectObjByMap(Map params) {
        return null;
    }

    @Override
    public boolean writeArp() {
        try {
            this.arpMapper.writeArp();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.arpMapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void gatherArp(Date date){
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0){

            this.ipv4Service.truncateTable();
            this.ipv6Service.truncateTable();

            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "getarp.py";
//                String result = PythonExecUtils.exec(path);
                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = PythonExecUtils.exec(path, params);
                if(!"".equals(result)){
                    try {
                        List<Ipv4> array = JSONObject.parseArray(result, Ipv4.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                e.setAddTime(date);
                                this.ipv4Service.save(e);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                path = Global.PYPATH +  "getarpv6.py";
//                result = PythonExecUtils.exec(path);
                String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                result = PythonExecUtils.exec(path, params2);
                if(!"".equals(result)){
                    try {
                        List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                e.setAddTime(date);
                                this.ipv6Service.save(e);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 去重
        this.ipv4Service.removeDuplicates();
        this.ipv6Service.removeDuplicates();

        this.arpMapper.truncateTable();

        // 去重前arp ipv-ipv6
        this.writerArp(date);

        // 去重后arp
        // 写入ipv4数据，写入ipv6数据
        this.arpMapper.writeArp();


    }

    public void writerArp(Date date){
        List<Arp> arps = this.arpMapper.joinSelectObjAndIpv6();
        if(arps.size() > 0) {
            for (Arp arp : arps) {
                arp.setAddTime(date);
                List<Ipv6> ipv6s = arp.getIpv6List();
                if (ipv6s.size() > 0) {
                    if (ipv6s.size() == 1) {
                        arp.setV6ip(ipv6s.get(0).getIp());
                    } else {
                        for (int i = 0; i < ipv6s.size(); i++) {
                            String v6ip = "v6ip";
                            if(i > 0){
                                v6ip = "v6ip" + i;
                            }
                            Field[] fields = Arp.class.getDeclaredFields();
                            for (Field field : fields) {
                                String propertyName = field.getName(); // 获取属性名

                                // 设置属性值为"Hello World!"
                                if (v6ip.equalsIgnoreCase(propertyName)) {
                                    field.setAccessible(true); // 若属性为private或protected需要先调用此方法进行访问控制的关闭
                                    try {
                                        field.set(arp, ipv6s.get(i).getIp());
                                        break;
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
                this.arpMapper.save(arp);
            }
        }
    }

}
