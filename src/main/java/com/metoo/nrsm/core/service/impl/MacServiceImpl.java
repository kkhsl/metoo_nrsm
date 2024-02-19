package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.mapper.MacMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.entity.nspm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-02 10:20
 */
@Service
@Transactional
public class MacServiceImpl implements IMacService {

    @Autowired
    private MacMapper macMapper;
    @Autowired
    private IArpService arpService;
    @Autowired
    private INetworkElementService networkElementService;

    @Override
    public boolean save(Mac instance) {
        try {
            this.macMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncateTable() {
        try {
            this.macMapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String getlldp =  "[{\"hostname\": \"core_sw2\", \"localport\": \"GigabitEthernet0/1\", \"remoteport\": \"GigabitEthernet0/1\"}, {\"hostname\": \"jr_sw3\", \"localport\": \"GigabitEthernet0/5\", \"remoteport\": \"GigabitEthernet0/1\"}, {\"hostname\": \"jr_sw2\", \"localport\": \"GigabitEthernet0/2\", \"remoteport\": \"GigabitEthernet0/2\"}, {\"hostname\": \"jr_sw4\", \"localport\": \"GigabitEthernet0/3\", \"remoteport\": \"GigabitEthernet0/3\"}]";

        String a = "[\"a\",\"b\"]";
//        List list = Arrays.asList(getlldp);
//        System.out.println(list);
        List lldps = JSONObject.parseArray(getlldp, Map.class);
        System.out.println(lldps);
    }

    @Override
    public void gatherMac(Date date){
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0){
            this.macMapper.truncateTable();
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

                                    this.macMapper.save(e);
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
                                    this.macMapper.save(e);
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


    // mac对端设备
//    @Deprecation
//    public void setRemoteDevice(Mac e, Map de_weight, List<Map> lldps){
//        // 写入对端信息
//        if(de_weight.get(e.getPort()) == null || de_weight.get(e.getPort()).equals("")){
//            if(lldps != null && lldps.size() > 0){
//                for(Map<String, String> obj : lldps){
//                    String localport = obj.get("localport").toString();
//                    if(localport.equals(e.getPort())){
//                        e.setRemote_port(obj.get("remoteport").toString());
//                        e.setRemote_device(obj.get("hostname").toString());
//                        de_weight.put(e.getPort(), 1);
//                    }
//                }
//            }
//        }
//    }

    // mac对端设备
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
                this.macMapper.save(mac);
            }
        }
    }

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

    // 将arp表中mac对应的ip地址写入mac表：mac+port+deviceip
    public String getArpIp(String mac, String port, String deviceIp){
        Map params = new HashMap();
        List<Arp> arps = this.arpService.selectObjByMap(params);
        if(arps.size() > 0){
            return arps.get(0).getDeviceIp();
        }
        return "";
    }
}
