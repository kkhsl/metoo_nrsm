package com.metoo.nrsm.core.utils.gather.gathermac;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.IMacService;
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

/**
 * 优化
 * 1. 减少重复的 try-catch 块：合并多个重复的 try-catch 代码，避免重复处理错误。
 * 2. 重构 Python 脚本执行：Python 脚本的执行逻辑重复多次，可以将其提取成一个公共方法，减少代码重复。
 * 3. 高效处理大数据集：对于大数据集，使用线程安全的列表，并在数据量达到一定阈值时进行批量插入，如评论中提到的优化建议。
 * 4. 改进流程控制：去除不必要的嵌套条件，简化逻辑，使代码更易于维护。
 * 5. 日志和错误处理：增强日志记录，添加具体的错误信息，以便在出现问题时能快速定位问题。
 */
@Slf4j
@Component
public class GatherSingleThreadingMacUtilsBack {

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

            // mac 复制数据、写入标签、ip地址信息等
            this.gatherMacUtils.copyGatherData(date);

            try {
                this.terminalService.syncTerminal(date);
//                this.terminalService.v4Tov6Terminal(date);
                // 统计终端属于哪个单位
                this.terminalService.writeTerminalUnit();

                this.terminalService.writeTerminalUnitV6();
                // 根据vendor判断终端类型
                this.terminalService.writeTerminalType();
                // 统计终端是否属于双栈终端
                this.terminalService.dualStackTerminal();

                // 统计终端ip数量
                processTerminalCount(date);

                // 同步终端到终端历史表
                this.terminalService.syncTerminalToTerminalHistory();

            } catch (Exception e) {
                e.printStackTrace();
            }

            this.macService.truncateTableGather();

            PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

            for (NetworkElement networkElement : networkElements) {
                if(StringUtils.isBlank(networkElement.getVersion())
                        || StringUtils.isBlank(networkElement.getCommunity())){
                    continue;
                }

                String hostname = execPythonScript(pythonExecUtils, "gethostname.py", networkElement);

                if (StringUtils.isNotEmpty(hostname)) {

                    processLLDP(pythonExecUtils, networkElement, hostname, date);
                    processMac(pythonExecUtils, networkElement, hostname, date);
                    processPortMac(pythonExecUtils, networkElement, hostname, date);

                }
            }
        }
    }

    private void processMac(PythonExecUtils pythonExecUtils, NetworkElement networkElement, String hostname, Date date) {
        log.info("getmac.py ===== {}", networkElement.getIp());
        String getmac = execPythonScript(pythonExecUtils, "getmac.py", networkElement);
        if (StringUtil.isNotEmpty(getmac)) {
            try {
                List<Mac> macList = JSONObject.parseArray(getmac, Mac.class);
                List<Mac> filteredList = filterMacData(macList, networkElement, hostname, date);
                if (!filteredList.isEmpty()) {
                    macService.batchSaveGather(filteredList);
                }
            } catch (Exception e) {
                log.error("处理MAC数据时出错，设备：{}", networkElement.getIp(), e);
            }
        }
    }

    private List<Mac> filterMacData(List<Mac> macList, NetworkElement networkElement, String hostname, Date date) {
        List<Mac> filteredList = new ArrayList<>();
        for (Mac e : macList) {
            if ("3".equals(e.getType())) {
                e.setAddTime(date);
                e.setDeviceIp(networkElement.getIp());
                e.setDeviceName(networkElement.getDeviceName());
                e.setHostname(hostname);
                if (parseLineBeginWith(e.getMac(), "^00:00:5e")) {
                    e.setTag("LV");
                }
                filteredList.add(e);
            }
        }
        return filteredList;
    }

    private String execPythonScript(PythonExecUtils pythonExecUtils, String scriptName, NetworkElement networkElement) {
        String scriptPath = Global.PYPATH + scriptName;
        String[] params = {networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity()};
        return pythonExecUtils.exec2(scriptPath, params);
    }

    private void processTerminalCount(Date date) {
        try {
            Map<String, Integer> terminal = this.terminalService.terminalCount();
            TerminalCount count = new TerminalCount();
            count.setAddTime(date);
            count.setV4ip_count(terminal != null ? Integer.parseInt(String.valueOf(terminal.get("v4ip_count"))) : 0);
            count.setV6ip_count(terminal != null ? Integer.parseInt(String.valueOf(terminal.get("v6ip_count"))) : 0);
            count.setV4ip_v6ip_count(terminal != null ? Integer.parseInt(String.valueOf(terminal.get("v4ip_v6ip_count"))) : 0);
            terminalCountService.save(count);
        } catch (NumberFormatException e) {
            log.error("处理终端统计时出错", e);
        }
    }

    private void processLLDP(PythonExecUtils pythonExecUtils, NetworkElement networkElement, String hostname, Date date) {
        log.info("getlldp.py ===== {}", networkElement.getIp());
        String getlldp = execPythonScript(pythonExecUtils, "getlldp.py", networkElement);
        if (StringUtil.isNotEmpty(getlldp)) {
            List<Map> lldps = JSONObject.parseArray(getlldp, Map.class);
            this.setRemoteDevice(networkElement, lldps, hostname, date);
        }
    }

    private void processPortMac(PythonExecUtils pythonExecUtils, NetworkElement networkElement, String hostname, Date date) {
        log.info("getportmac.py ===== {}", networkElement.getIp());
        String getportmac = execPythonScript(pythonExecUtils, "getportmac.py", networkElement);
        if (StringUtil.isNotEmpty(getportmac)) {
            try {
                List<Mac> macList = JSONObject.parseArray(getportmac, Mac.class);
                List<Mac> filteredList = filterPortMacData(macList, networkElement, hostname, date);
                if (!filteredList.isEmpty()) {
                    macService.batchSaveGather(filteredList);
                }
            } catch (Exception e) {
                log.error("处理端口MAC数据时出错，设备：{}", networkElement.getIp(), e);
            }
        }
    }

    private List<Mac> filterPortMacData(List<Mac> macList, NetworkElement networkElement, String hostname, Date date) {
        List<Mac> filteredList = new ArrayList<>();
        for (Mac e : macList) {
            if ("1".equals(e.getStatus())) { // up 状态
                e.setAddTime(date);
                e.setDeviceIp(networkElement.getIp());
                e.setDeviceName(networkElement.getDeviceName());
                e.setTag("L");
                e.setHostname(hostname);
                if (parseLineBeginWith(e.getMac(), "^00:00:5e")) {
                    e.setTag("LV");
                }
                filteredList.add(e);
            }
        }
        return filteredList;
    }

    /**
     * 判断mac是否以某个规则开始
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