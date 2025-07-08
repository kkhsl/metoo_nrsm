package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.ArpMapper;
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.Ipv6Service;
import com.metoo.nrsm.entity.Arp;
import com.metoo.nrsm.entity.Ipv6;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.lang.reflect.Field;
import java.util.*;

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
    public List<Arp> selectObjDistinctV4ip() {
        return this.arpMapper.selectObjDistinctV4ip();
    }

    @Override
    public List<Arp> selectObjByMap(Map params) {
        return this.arpMapper.selectObjByMap(params);
    }

    @Override
    public List<Arp> getDeviceArpByUuid(String uuid) {
        Map params = new HashMap();
        params.put("uuid", uuid);
        List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
        if(networkElements.size() > 0){
            NetworkElement networkElement = networkElements.get(0);
            Map map=new HashMap();
            map.put("v4ip",networkElement.getIp());
            List<Arp> arps = arpMapper.selectObjByMap(map);
            if(arps.size() > 0){
                return arps;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Arp> joinSelectObjAndIpv6() {
        return this.arpMapper.joinSelectObjAndIpv6();
    }

    @Override
    public List<Arp> mergeIpv4AndIpv6(Map params) {
        return this.arpMapper.mergeIpv4AndIpv6(params);
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
    public boolean save(Arp instance) {
        try {
            this.arpMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean deleteTable() {
        try {
            this.arpMapper.deleteTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTableGather() {
        try {
            this.arpMapper.truncateTableGather();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveGather(Arp instance) {
        try {
            this.arpMapper.saveGather(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean batchSaveGather(List<Arp> instance) {
        try {
            this.arpMapper.batchSaveGather(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchSaveGatherBySelect(Map params) {
        try {
            this.arpMapper.batchSaveGatherBySelect(params);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchSaveIpV4AndIpv6ToArpGather(Map params) {
        try {
            this.arpMapper.batchSaveIpV4AndIpv6ToArpGather(params);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyGatherDataToArp() {
        try {
            this.arpMapper.deleteTable();// 使用创建新表替换旧表的方式替换deleteTable
            this.arpMapper.copyGatherDataToArp();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    @Override
    public boolean gatherArp(Date date) {
        try {
            this.arpMapper.gathreArp(date);
            this.copyGatherDataToArp();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
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
