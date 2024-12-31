package com.metoo.nrsm.core.utils.gather.gathermac;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalCountService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.service.impl.MacServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.TerminalCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-23 10:39
 */
@Slf4j
@Component
public class GatherSingleThreadingMacUtils {

    @Autowired
    private IMacService macService;
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private ITerminalCountService terminalCountService;
    @Autowired
    private INetworkElementService networkElementService;

    // 单线程采集
    public void gatherMac(List<NetworkElement> networkElements, Date date) {
        if (networkElements.size() > 0) {

            // mac 复制数据、写入标签、ip地址信息等
            this.gatherMacUtils.copyGatherData(date);

            try {
                this.terminalService.syncTerminal(date);

                // nswitch分析-vm
                this.terminalService.updateVMHostDeviceType();

                this.terminalService.updateVMDeviceType();

                this.terminalService.updateVMDeviceIp();

                this.networkElementService.updateObjDisplay();
//
//                this.terminalService.v4Tov6Terminal(date);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 标记deviceType是否为设备。（v4ip为空，根据mac地址判断）
            this.terminalService.updateObjDeviceTypeByMac();


          // 统计终端属于哪个单位
            try {
                this.terminalService.writeTerminalUnit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                this.terminalService.writeTerminalUnitV6();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 根据vendor判断终端类型
            this.terminalService.writeTerminalType();

            this.terminalService.writeTerminalDeviceTypeToVendor();

            // 统计终端是否属于双栈终端
            this.terminalService.dualStackTerminal();

            // 统计终端ip数量
            try {
                Map terminal = this.terminalService.terminalCount();
                if (terminal != null) {
                    TerminalCount count = new TerminalCount();
                    count.setAddTime(date);
                    count.setV4ip_count(Integer.parseInt(String.valueOf(terminal.get("v4ip_count"))));
                    count.setV6ip_count(Integer.parseInt(String.valueOf(terminal.get("v6ip_count"))));
                    count.setV4ip_v6ip_count(Integer.parseInt(String.valueOf(terminal.get("v4ip_v6ip_count"))));
                    terminalCountService.save(count);
                } else {
                    TerminalCount count = new TerminalCount();
                    count.setAddTime(date);
                    count.setV4ip_count(0);
                    count.setV6ip_count(0);
                    count.setV4ip_v6ip_count(0);
                    terminalCountService.save(count);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            // 同步终端到终端历史表
            this.terminalService.syncTerminalToTerminalHistory();

            this.macService.truncateTableGather();

            for (NetworkElement networkElement : networkElements) {

                if(StringUtils.isBlank(networkElement.getVersion())
                        || StringUtils.isBlank(networkElement.getCommunity())){
                    continue;
                }

                String gethostnamePath = Global.PYPATH + "gethostname.py";

                PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

                String[] gethostnameParams = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};

                String hostname = pythonExecUtils.exec2(gethostnamePath, gethostnameParams);

                if (StringUtils.isNotEmpty(hostname)) {

                    log.info("getlldp.py ===== {}", networkElement.getIp());

                    String getlldpPath = Global.PYPATH + "getlldp.py";
                    String[] getlldpParams = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};

                    String getlldp = pythonExecUtils.exec2(getlldpPath, getlldpParams);
                    if (StringUtil.isNotEmpty(getlldp)) {
                        List<Map> lldps = JSONObject.parseArray(getlldp, Map.class);
                        this.setRemoteDevice(networkElement, lldps, hostname, date);
                    }

                    log.info("getmac.py ====={}", networkElement.getIp());

                    String getMacPath = Global.PYPATH + "getmac.py";
                    String[] getMacParams = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String getmac = pythonExecUtils.exec2(getMacPath, getMacParams);
                    if (StringUtil.isNotEmpty(getmac)) {
                        try {
                            List<Mac> macList = JSONObject.parseArray(getmac, Mac.class);
                            if (macList.size() > 0) {
                                List<Mac> list = new ArrayList();
                                MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
                                macList.forEach(mac -> {
                                    if ("3".equals(mac.getType())) {
                                        mac.setAddTime(date);
                                        mac.setDeviceIp(networkElement.getIp());
                                        mac.setDeviceName(networkElement.getDeviceName());
                                        mac.setDeviceUuid(networkElement.getUuid());
                                        mac.setHostname(hostname);
                                        if(StringUtils.isNotEmpty(mac.getMac())){
                                            mac.setMac1(MyStringUtils.getSubstringBeforNthDelimiter(mac.getMac(), ":", 3));
                                        }
                                        String patten = "^" + "00:00:5e";
                                        boolean flag = this.parseLineBeginWith(mac.getMac(), patten);
                                        if (flag) {
                                            mac.setTag("LV");
                                        }
                                        list.add(mac);
                                    }
                                });
                                if (list.size() > 0) {
                                    macService.batchSaveGather(list);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    log.info("getportmac.py ====={}", networkElement.getIp());

                    String path4 = Global.PYPATH + "getportmac.py";
                    String[] params4 = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String getportmac = pythonExecUtils.exec2(path4, params4);
                    if (StringUtil.isNotEmpty(getportmac)) {
                        try {
                            List<Mac> array = JSONObject.parseArray(getportmac, Mac.class);
                            if (array.size() > 0) {
                                List<Mac> list = new ArrayList();
                                MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
                                array.forEach(e -> {
                                    if ("1".equals(e.getStatus())) {// up状态
                                        e.setAddTime(date);
                                        e.setDeviceIp(networkElement.getIp());
                                        e.setDeviceName(networkElement.getDeviceName());
                                        e.setDeviceUuid(networkElement.getUuid());
                                        e.setTag("L");
                                        e.setHostname(hostname);
                                        if(StringUtils.isNotEmpty(e.getMac())){
                                            e.setMac1(MyStringUtils.getSubstringBeforNthDelimiter(e.getMac(), ":", 3));
                                        }
                                        String patten = "^" + "00:00:5e";
                                        boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                                        if (flag) {
                                            e.setTag("LV");
                                        }
                                        list.add(e);
                                    }
                                });

                                /**
                                 * 优化方案：
                                 *      如果单台数据量过大，可以使用线程安全list，当list集合数据超过指定数据，在进行批量插入
                                 *      三台线程同时采集（定义线程安全方法Guarded）
                                 *
                                 */

                                if (list.size() > 0) {
                                    macService.batchSaveGather(list);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    public void setRemoteDevice(NetworkElement e, List<Map> lldps, String hostname, Date date){
        // 写入对端信息
        if(lldps != null && lldps.size() > 0){

            MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
            List<Mac> list = new ArrayList();
            Map params = new HashMap();
            for(Map<String, String> obj : lldps){
                Mac mac = new Mac();
                if(StringUtils.isNotEmpty(e.getDeviceName())){
                    params.clear();
                    params.put("deviceIp", e.getIp());
                    List<NetworkElement> networkElements = this.networkElementService.selectObjByMap(params);
                    if(!networkElements.isEmpty()){
                        mac.setDeviceUuid(networkElements.get(0).getUuid());
                    }
                }

                mac.setAddTime(date);
                mac.setDeviceIp(e.getIp());
                mac.setDeviceName(e.getDeviceName());
                mac.setDeviceUuid(e.getUuid());
//                mac.setPort(e.getPort());
                mac.setMac("00:00:00:00:00:00");
                mac.setHostname(hostname);
                mac.setTag("DE");
                mac.setRemotePort(obj.get("remoteport"));
                mac.setRemoteDevice(obj.get("hostname"));
                if(StringUtils.isNotEmpty(mac.getMac())){
                    mac.setMac1(MyStringUtils.getSubstringBeforNthDelimiter(mac.getMac(), ":", 3));
                }
//                macService.save(mac);
                list.add(mac);
            }
            if(list.size() > 0){
                macService.batchSaveGather(list);
            }
        }
    }

}