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
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.TerminalCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    // 单线程采集
    public void gatherMac(List<NetworkElement> networkElements, Date date) {
        if (networkElements.size() > 0) {

            // mac 复制数据写入标签和ip地址信息等
            this.gatherMacUtils.copyGatherData(date);

            try {
                this.terminalService.syncTerminal(date);
//                this.terminalService.v4Tov6Terminal(date);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 统计终端属于哪个单位
            //...
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

                String path = Global.PYPATH + "gethostname.py";

                PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String hostname = pythonExecUtils.exec2(path, params);
                if (StringUtils.isNotEmpty(hostname)) {

                    PythonExecUtils pythonExecUtils2 = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");
                    String path2 = Global.PYPATH + "getlldp.py";
                    String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String result = pythonExecUtils2.exec2(path2, params2);
                    if (StringUtil.isNotEmpty(result)) {
                        List<Map> lldps = JSONObject.parseArray(result, Map.class);
                        this.setRemoteDevice(networkElement, lldps, hostname, date);
                    }

                    PythonExecUtils pythonExecUtils3 = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");
                    String path3 = Global.PYPATH + "getmac.py";
                    String[] params3 = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String result3 = pythonExecUtils3.exec2(path3, params3);
                    if (StringUtil.isNotEmpty(result3)) {
                        try {
                            List<Mac> array = JSONObject.parseArray(result3, Mac.class);
                            if (array.size() > 0) {
                                List<Mac> list = new ArrayList();
                                MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
                                array.forEach(e -> {
                                    if ("3".equals(e.getType())) {
                                        e.setAddTime(date);
                                        e.setDeviceIp(networkElement.getIp());
                                        e.setDeviceName(networkElement.getDeviceName());
                                        e.setHostname(hostname);
                                        String patten = "^" + "00:00:5e";
                                        boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                                        if (flag) {
                                            e.setTag("LV");
                                        }
                                        list.add(e);
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

                    PythonExecUtils pythonExecUtils4 = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");
                    String path4 = Global.PYPATH + "getportmac.py";
                    String[] params4 = {networkElement.getIp(), networkElement.getVersion(),
                            networkElement.getCommunity()};
                    String result4 = pythonExecUtils4.exec2(path4, params4);
                    if (StringUtil.isNotEmpty(result4)) {
                        try {
                            List<Mac> array = JSONObject.parseArray(result4, Mac.class);
                            if (array.size() > 0) {
                                List<Mac> list = new ArrayList();
                                MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
                                array.forEach(e -> {
                                    if ("1".equals(e.getStatus())) {// up状态
                                        e.setAddTime(date);
                                        e.setDeviceIp(networkElement.getIp());
                                        e.setDeviceName(networkElement.getDeviceName());
                                        e.setTag("L");
                                        e.setHostname(hostname);
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
            for(Map<String, String> obj : lldps){
                Mac mac = new Mac();
                mac.setAddTime(date);
                mac.setDeviceIp(e.getIp());
                mac.setDeviceName(e.getDeviceName());
//                mac.setPort(e.getPort());
                mac.setMac("00:00:00:00:00:00");
                mac.setHostname(hostname);
                mac.setTag("DE");
                mac.setRemotePort(obj.get("remoteport"));
                mac.setRemoteDevice(obj.get("hostname"));
//                macService.save(mac);
                list.add(mac);
            }
            if(list.size() > 0){
                macService.batchSaveGather(list);
            }
        }
    }

}