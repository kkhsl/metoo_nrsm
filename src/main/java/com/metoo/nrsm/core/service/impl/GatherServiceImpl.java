package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.mapper.ArpMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.core.utils.gather.thread.GatherIpV4Runnable;
import com.metoo.nrsm.core.utils.gather.thread.GatherIpV6Runnable;
import com.metoo.nrsm.entity.nspm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 10:29
 */
@Service
public class GatherServiceImpl implements IGatherService {

    @Autowired
    private IArpService arpService;
    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private Ipv6Service ipv6Service;
    @Autowired
    private IMacService macService;
    @Autowired
    private INetworkElementService networkElementService;

    @Override
    public void gatherMac(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0){
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
                    this.setRemoteDevice(networkElement, lldps, hostname, date);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                path = Global.PYPATH + "getmac.py";
                // String result = PythonExecUtils.exec(path);
                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = PythonExecUtils.exec(path, params);
                if(!"".equals(result)){
                    try {


                        List<Mac> array = JSONObject.parseArray(result, Mac.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                if("3".equals(e.getType())){
                                    e.setDeviceIp(networkElement.getIp());
                                    e.setDeviceName(networkElement.getDeviceName());
                                    e.setAddTime(date);
                                    e.setHostname(hostname);
//                                    e.setTag("L");
                                    String patten = "^" + "00:00:5e";
                                    boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                                    if(flag){
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
                if(!"".equals(result)){
                    try {
                        List<Mac> array = JSONObject.parseArray(result, Mac.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                if("1".equals(e.getStatus())){
                                    e.setAddTime(date);
                                    e.setDeviceIp(networkElement.getIp());
                                    e.setDeviceName(networkElement.getDeviceName());
                                    e.setTag("L");
                                    e.setHostname(hostname);
                                    String patten = "^" + "00:00:5e";
                                    boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                                    if(flag){
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

    /**
     * 判断Mac是否以某个规则开始
     * @param lineText
     * @param head
     * @return
     */
    public boolean parseLineBeginWith(String lineText, String head){

        if(StringUtil.isNotEmpty(lineText) && StringUtil.isNotEmpty(head)){
            String patten = "^" + head;

            Pattern compiledPattern = Pattern.compile(patten);

            Matcher matcher = compiledPattern.matcher(lineText);

            while(matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * mac对端设备
     * @param e
     * @param lldps
     * @param hostname
     * @param date
     */
    public void setRemoteDevice(NetworkElement e, List<Map> lldps, String hostname, Date date){
        // 写入对端信息
        if(lldps != null && lldps.size() > 0){
            for(Map<String, String> obj : lldps){
                Mac mac = new Mac();
                mac.setAddTime(date);
                mac.setDeviceIp(e.getIp());
                mac.setDeviceName(e.getDeviceName());
//                mac.setPort(e.getPort());
                mac.setMac("00:00:00:00:00:00");
                mac.setHostname(hostname);
                mac.setTag("DE");
                mac.setRemote_port(obj.get("remoteport"));
                mac.setRemote_device(obj.get("hostname"));
                this.macService.save(mac);
            }
        }
    }

//
//    @Override
//    public void gatherArp(Date date) {
//        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
//        if(networkElements.size() > 0){
//
//            this.ipv4Service.truncateTable();
//            this.ipv6Service.truncateTable();
//
//            for (NetworkElement networkElement : networkElements) {
//                String path = Global.PYPATH + "getarp.py";
////                String result = PythonExecUtils.exec(path);
//                String[] params = {networkElement.getIp(), networkElement.getVersion(),
//                        networkElement.getCommunity()};
//                String result = PythonExecUtils.exec(path, params);
//                if(!"".equals(result)){
//                    try {
//                        List<Ipv4> array = JSONObject.parseArray(result, Ipv4.class);
//                        if(array.size()>0){
//                            array.forEach(e -> {
//                                e.setDeviceIp(networkElement.getIp());
//                                e.setDeviceName(networkElement.getDeviceName());
//                                e.setAddTime(date);
//                                this.ipv4Service.save(e);
//                            });
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                path = Global.PYPATH +  "getarpv6.py";
////                result = PythonExecUtils.exec(path);
//                String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
//                        networkElement.getCommunity()};
//                result = PythonExecUtils.exec(path, params2);
//                if(!"".equals(result)){
//                    try {
//                        List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
//                        if(array.size()>0){
//                            array.forEach(e -> {
//                                e.setDeviceIp(networkElement.getIp());
//                                e.setDeviceName(networkElement.getDeviceName());
//                                e.setAddTime(date);
//                                this.ipv6Service.save(e);
//                            });
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//        // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
//        this.ipv4Service.removeDuplicates();
//        this.ipv6Service.removeDuplicates();
//
//        this.arpMapper.truncateTable();
//
//        // 合并mac和port相同的数据（ipv4和ipv6）到arp表
//        this.writerArp(date);
//        // 排除上个步骤中相同的数据，写入arp表
//        this.arpMapper.writeArp();
//    }

    @Override
    public void gatherArp(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.arpService.truncateTableGather();
            // 合并mac和port相同的数据（ipv4和ipv6）到arp表
            this.writerArp(date);
            // 排除上个步骤中相同的数据，写入arp表
            this.arpService.writeArp();

            this.copyGatherDataToArp();
        }
    }

    public void writerArp(Date date){
        List<Arp> arps = this.arpService.joinSelectObjAndIpv6();
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
                this.arpService.saveGather(arp);
            }
//            this.arpService.batchSaveGather(arps);
        }
    }

    // 复制采集数据到ipv4表
    public void copyGatherDataToArp(){
        try {
            this.arpService.copyGatherDataToArp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gatherIpv4(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            this.ipv4Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
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
                            this.ipv4Service.batchSaveGather(ipv4s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 非多线程，单线程串行情况下可放到最后执行
            this.copyGatherDataToIpv4();
            this.removeDuplicatesIpv4();
        }
    }

    @Override
    public void gatherIpv4Thread(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            // （采集结束，复制到ipv4表中）
            // 多线程并行执行，避免出现采集未结束时，执行copy操作没有数据，所以将复制数据操作，放到采集前，复制上次采集结果即可
            this.copyGatherDataToIpv4();
            this.removeDuplicatesIpv4();
            this.ipv4Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                Thread thread = new Thread(new GatherIpV4Runnable(networkElement, date));
                thread.start();
            }
        }
    }

    // 复制采集数据到ipv4表
    public void copyGatherDataToIpv4(){
        try {
//            this.ipv4Service.truncateTable();
            this.ipv4Service.copyGatherToIpv4();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 去重ipv4重复数据到ipv4_mirror(组合arp表)
    public void removeDuplicatesIpv4(){
        try {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.ipv4Service.removeDuplicates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void gatherIpv6(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            this.ipv6Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "getarp.py";
                path = Global.PYPATH +  "getarpv6.py";
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
                        this.ipv6Service.batchSaveGather(array);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // 非多线程，单线程串行情况下可放到最后执行
            this.copyGatherDataToIpv6();
            this.removeDuplicatesIpv6();
        }
    }


    @Override
    public void gatherIpv6Thread(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            // 非多线程，单线程串行情况下可放到最后执行
            this.copyGatherDataToIpv6();
            this.removeDuplicatesIpv6();

            this.ipv6Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                Thread thread = new Thread(new GatherIpV6Runnable(networkElement, date));
                thread.start();
            }
        }
    }

    // 复制采集数据到ipv6表
    public void copyGatherDataToIpv6(){
        try {
            this.ipv6Service.copyGatherToIpv6();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 去重ipv4重复数据，组合arp表
    public void removeDuplicatesIpv6(){
        try {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.ipv6Service.removeDuplicates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
