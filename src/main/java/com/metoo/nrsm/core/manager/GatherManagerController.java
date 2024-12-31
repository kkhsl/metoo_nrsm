package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
import com.metoo.nrsm.core.mapper.TerminalMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@Slf4j
@RequestMapping("/admin/gather")
@RestController
public class GatherManagerController {

    @Autowired
    private IGatherService gatherService;
    @Autowired
    private GatherMacUtils gatherMacUtils;
    @Autowired
    private SnmpStatusUtils snmpStatusUtils;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private IGradWeightService gradWeightService;
    @Autowired
    private IFluxDailyRateService fluxDailyRateService;
    @Autowired
    private PythonExecUtils pythonExecUtils;
    @Autowired
    private TerminalMapper terminalMapper;
    @Autowired
    private IProbeService probeService;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private INetworkElementService networkElementService;


    @GetMapping("/insertTerminal")
    public void insertTerminal(){
        try {
            this.terminalService.syncTerminal(new Date());

            // nswitch分析-vm
            this.terminalService.updateVMHostDeviceType();

            this.terminalService.updateVMDeviceType();

            this.terminalService.updateVMDeviceIp();

            this.networkElementService.updateObjDisplay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/terminal/device/type")
    public String updateDeviceType(){
        this.terminalService.updateObjDeviceTypeByMac();
        return "ok";
    }


    @GetMapping("/vm/mac/terminal")
    public String mac_terminal(){

    // mac 复制数据、写入标签、ip地址信息等
        this.gatherMacUtils.copyGatherData(new Date());

        try {
            this.terminalService.syncTerminal(new Date());

            // nswitch分析-vm
            this.terminalService.updateVMHostDeviceType();

            this.terminalService.updateVMDeviceType();

            this.terminalService.updateVMDeviceIp();

            this.networkElementService.updateObjDisplay();

//                this.terminalService.v4Tov6Terminal(date);
    } catch (Exception e) {
        e.printStackTrace();
    }
        return "ok";
    }

    @GetMapping("/vm/copyGatherData")
    public String copyGatherData(){
        gatherMacUtils.copyGatherData(new Date());
        return "ok";
    }

    @GetMapping("/vm/syncTerminal")
    public String syncTerminal(){
        this.terminalService.syncTerminal(new Date());
        return "ok";
    }


    @GetMapping("/vm/devicetype")
    public String devicetype(){

        terminalService.updateVMHostDeviceType();
        terminalService.updateVMDeviceType();
        return "ok";
    }

    @GetMapping("/vm")
    public String vm(){
        terminalService.updateVMDeviceIp();
        return "ok";
    }


    @GetMapping("/nswitch")
    public String nswitch(){
        gatherMacUtils.rtTovdt(new Date());
        return "ok";
    }


    @GetMapping("/probe/start")
    public String start_probe(){
        probeService.scanByTerminal();
        return "ok";
    }

    @GetMapping("/scanByWriteTerminal")
    public void writeTerminal(){
        List<Terminal> terminals = this.terminalService.selectObjByMap(Collections.EMPTY_MAP);
        List<Probe> probes = this.probeService.mergeProbesByIp();
        if(probes.isEmpty() || terminals.isEmpty()){
            return;
        }
        Map<String, Probe> map = new HashMap<>();
        for (Probe probe : probes) {
            map.put(probe.getIp_addr(), probe);
        }
        outerLoop: // 给外层循环加个标签
        for (Terminal terminal : terminals) {
            Probe probe = map.get(terminal.getV4ip());
            if(probe != null){
                boolean device = false;
                List list = new ArrayList();
                String combined = probe.getCombined();
                String[] combineds = combined.split(",");
                if(combineds.length > 0){
                    for (String ele : combineds) {
                        Map stats = new HashMap();
                        String[] eles = ele.split("/", 2);// 字符串的末尾或连续分隔符之间可能会包括一个分隔符本身
                        if(eles.length > 0){
                            String port_num = eles[0];
                            if(port_num.equals("2")){
//                                continue outerLoop; // 使用标签跳出外层循环
                                continue;
                            }
                            String application_protocol = eles[1];
                            if(application_protocol.contains("telnet")){
                                device = true;
                                continue outerLoop; // 使用标签跳出外层循环
                            }
                            stats.put("port_num", port_num);
                            stats.put("application_protocol", application_protocol);
                            list.add(stats);
                        }
                    }
                }
                boolean flag = false;

                String combined_os = probe.getCombined_os();
                String combined_ttl = probe.getCombined_ttl();
                if(StringUtils.isNotBlank(combined_ttl)){
                    String[] ttls = combined_ttl.split(",");
                    if(ttls.length > 0){
                        for (String ttl : ttls) {
                            if(Integer.parseInt(ttl) > 120 && Integer.parseInt(ttl) < 129){
                                if(StringUtil.isEmpty(combined_os)){
                                    combined_os = "Windows";
                                    flag = true;
                                    break;
                                }
                            }else if(Integer.parseInt(ttl) > 200){
                                flag = true;
                                device = true;
//                                combined_os = "device";
                                break;
                            }
                        }
                    }
                }
                List<JSONObject> osList = new ArrayList();
                if(StringUtils.isNotEmpty(combined_os)){
                    if(!flag){
                        osList = parseInputToJsonList(combined_os);
                    }else{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("vendor", combined_os);
                        osList.add(jsonObject);
                    }
                }
                if(osList.size() > 0){
                    JSONObject jsonObject = osList.get(0);
                    terminal.setOs(jsonObject.getString("vendor"));

                }

                if(probe.getCombined_vendor().toLowerCase().contains("Ruijie".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("Tenda".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("h3c".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("TP-LINK".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("mercury".toLowerCase()) ||
                        probe.getCombined_vendor().toLowerCase().contains("Device".toLowerCase())){
                    device = true;
                }
                if(probe.getCombined_application_protocol().toLowerCase().contains("telnet".toLowerCase())){
                    device = true;
                }

                terminal.setCombined_vendor_gen_family(JSONObject.toJSONString(osList));
                terminal.setCombined_port_protocol(JSONObject.toJSONString(list));
                if(device){
                    terminal.setDeviceType(1);
                }
                this.terminalService.update(terminal);
            }
        }


        // 更新nswitch终端设备ip
        List<Terminal> terminalList = this.terminalService.selectDeviceIpByNSwitch();
        if(!terminalList.isEmpty()){
            for (Terminal terminal : terminalList) {
                this.terminalService.update(terminal);
            }
        }
    }

    public static List<JSONObject> parseInputToJsonList(String input) {
        // 存储解析后的 JSON 对象列表
        List<JSONObject> jsonList = new ArrayList<>();

        if((input == null || !input.isEmpty()) && !input.contains(":")){
            return jsonList;
        }

        // 分割输入字符串，基于逗号分割多个数据项
        String[] items = input.split(",");

        // 遍历每个数据项
        for (String item : items) {
            // 按冒号分割
            String[] parts = item.split(":", 3);

            // 确保每个项包含三部分
            if (parts.length == 3) {
                String vendor = parts[0];
                String osGen = parts[1];
                String osFamily = parts[2];
                if(!StringUtils.isEmpty(vendor)
                        || !StringUtils.isEmpty(osGen) || !StringUtils.isEmpty(osFamily)){
                    // 创建 JSON 对象并存储数据
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("vendor", vendor);
                    jsonObject.put("os_gen", osGen);
                    jsonObject.put("os_family", osFamily);
                    // 将 JSON 对象添加到列表中
                    jsonList.add(jsonObject);
                }
            }
        }

        return jsonList;
    }


    @GetMapping("/scanByTerminal")
    public void scanByTerminal(){
        probeService.scanByTerminal();
    }


    @GetMapping("/gatherAll")
    public void gather(){
        log.info("dhcp");
        dhcpService.gather(DateTools.gatherDate());
        log.info("dhcp6");
        dhcp6Service.gather(DateTools.gatherDate());
        log.info("Ipv4Detail");
        gatherService.gatherIpv4Detail(DateTools.gatherDate());
        log.info("Port");
        gatherService.gatherPort(DateTools.gatherDate());
        log.info("PortIpv6");
        gatherService.gatherPortIpv6(DateTools.gatherDate());
        log.info("Ipv4");
        gatherService.gatherIpv4(DateTools.gatherDate());
        log.info("Ipv6");
        gatherService.gatherIpv6(DateTools.gatherDate());
        log.info("Arp");
        gatherService.gatherArp(DateTools.gatherDate());
        log.info("Mac");
        gatherService.gatherMac(DateTools.gatherDate());
    }


    @GetMapping("gatherSnmpStatus")
    public void gatherSnmpStatus(){
        this.gatherService.gatherSnmpStatus();
    }

    @GetMapping("arp")
    public void gatherArp(){
        this.gatherService.gatherArp(DateTools.gatherDate());
    }

    @GetMapping("ipv4")
    public void gatherIpv4(){
       this.gatherService.gatherIpv4(DateTools.gatherDate());
    }

    @GetMapping("ipv4/thread")
    public void gatherIpv4Thread(){
        this.gatherService.gatherIpv4Thread(DateTools.gatherDate());
    }

    @GetMapping("ipv6")
    public void gatherIpv6(){
        this.gatherService.gatherIpv6(DateTools.gatherDate());
    }

    @GetMapping("ipv6/thread")
    public void gatherIpv6Thread(){
        this.gatherService.gatherIpv6Thread(DateTools.gatherDate());
    }

    @GetMapping("mac")
    public void gatherMac(){
        Long time=System.currentTimeMillis();
        log.info("mac Start......");

        this.gatherService.gatherMac(DateTools.gatherDate());

        log.info("mac End......" + (System.currentTimeMillis()-time));
    }

    @GetMapping("mac/thread")
    public void gatherMacThread(){
        this.gatherService.gatherMacThread(DateTools.gatherDate());
    }


    @GetMapping("mac/mac_dt")
    public void mac_dt(){
        this.gatherMacUtils.mac_tag(DateTools.gatherDate());
    }

    @GetMapping("mac/tag_x")
    public void tag_x(){
        this.gatherMacUtils.tag_x();
    }

    @GetMapping("mac/tag_u")
    public void tag_u(){
        this.gatherMacUtils.tag_u();
    }

    @GetMapping("mac/tag_s")
    public void tag_s(){
        this.gatherMacUtils.tag_s();
    }

    @GetMapping("mac/tag_STOE")
    public void tag_STOE(){
        this.gatherMacUtils.tag_STOE();
    }


    @GetMapping("mac/copyArpMacAndIpToMac")
    public void copyArpMacAndIpToMac(){
        this.gatherMacUtils.copyArpMacAndIpToMac();
    }

    @GetMapping("mac/tag_XToE")
    public void tag_XToE(){
        this.gatherMacUtils.tag_XToE();
    }
    @GetMapping("mac/tag_UToE")
    public void tag_UToE(){
        this.gatherMacUtils.tag_UToE();
    }

    @GetMapping("mac/selectUToRT")
    public void selectUToUTByMap(){
        this.gatherMacUtils.selectUToRT();
    }

    @GetMapping("mac/RTToDT")
    public void RTToDT(){
        this.gatherMacUtils.RTToDT();
    }

    @GetMapping("mac/RTToDT2")
    public void RTToDT2(){
        this.gatherMacUtils.RTToDT();
    }

    @GetMapping("mac/copyArpIpToMacByDT")
    public void copyArpIpToMacByDT(){
        this.gatherMacUtils.copyArpIpToMacByDT();
    }


    @GetMapping("subnet")
    public void subnet(){
        this.gatherService.pingSubnet();
    }



    @Autowired
    private Ipv4Service ipv4Service;

    // 测试事务，tuncate后，是否导致脏读
    @GetMapping("testTruncate")
    public void testTruncate() throws InterruptedException {

//        this.ipv4Service.deleteTable();

//        this.ipv4Service.copyGatherToIpv4();

        this.gatherMacUtils.testTransaction(DateTools.gatherDate());

    }

    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;

    @GetMapping("terminal")
    public void terminal() {
        List<Terminal> terminals = this.terminalMapper.selectObjByMap(null);
        for (Terminal terminal : terminals) {
            terminal.setOnline(true);
            terminal.setDeviceTypeUuid("1");
        }
        this.terminalMapper.batchUpdate(terminals);
    }

    @GetMapping("dhcp")
    public void gatherDhcp() {
        dhcpService.gather(DateTools.gatherDate());
    }

    @GetMapping("dhcp6")
    public void dhcp6() {
        dhcp6Service.gather(DateTools.gatherDate());
    }

    @GetMapping("port")
    public void port() {
        gatherService.gatherPort(DateTools.gatherDate());
    }

    @GetMapping("portIpv6")
    public void portIpv6() {
        gatherService.gatherPortIpv6(DateTools.gatherDate());
    }

    @GetMapping("isIpv6")
    public void isIpv6() {
        gatherService.gatherIsIpv6(DateTools.gatherDate());
    }

    @GetMapping("flux")
    public void flux() {
        log.info("flux start......");
        gatherService.gatherFlux(DateTools.gatherDate());
        log.info("flux end......");
    }

    @GetMapping("getarpv6JSON")
    public void getarpv6JSON(String ip) {
        String path = Global.PYPATH +  "getarpv6.py";
        String[] params = {ip, "v2c",
                "public@123"};
        String result = pythonExecUtils.exec(path, params);
        if(StringUtil.isNotEmpty(result)) {
            try {
                List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
            } catch (Exception e) {
                e.printStackTrace();
                log.info(ip + " : " + result);
            }

        }}

    @GetMapping("selectObjLeftdifference")
    public Object selectObjLeftdifference() {
        List<Terminal> left = this.terminalMapper.selectObjLeftdifference();
        return left;
    }


    @GetMapping("ping")
    public void ping() {
        log.info("ping start......");
        gatherService.exec(DateTools.gatherDate());
        log.info("ping end......");
    }


    @GetMapping("/gatherHostName")
    public String gatherHostName() {
        String path = Global.PYPATH + "gethostname.py";
        String[] params = {"192.168.100.3", "v2c",
                "public@123"};
        String hostname = pythonExecUtils.exec(path, params);
        return hostname;
    }

    @GetMapping("clearAndcopyGatherDataToIpv4")
    public void clearAndcopyGatherDataToIpv4() {
        log.info("clearAndcopyGatherDataToIpv4......");
        ipv4Service.clearAndcopyGatherDataToIpv4();
        log.info("clearAndcopyGatherDataToIpv4......");
    }

    @GetMapping("/traffic")
    public void traffic(){
        GatherFactory factory = new GatherFactory();
        Gather gather = factory.getGather(Global.TRAFFIC);
        gather.executeMethod();
    }



    @GetMapping("fluxDailyRate")
    public void fluxDailyRate() {
        Date endOfDay = DateTools.getEndOfDay();
        FluxDailyRate fluxDailyRate = new FluxDailyRate();
        fluxDailyRate.setRate(new BigDecimal(0));
        fluxDailyRate.setAddTime(endOfDay);
        Map params = new HashMap();
        params.clear();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", endOfDay);
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        if(flowStatisticsList.size() > 0){
            BigDecimal sum = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                    .collect(Collectors.toList())
                    .stream().count();

            if(sum.compareTo(new BigDecimal(0)) >= 1){
                BigDecimal rate = sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                fluxDailyRate.setRate(rate);
                GradeWeight gradeWeight = this.gradWeightService.selectObjOne();
                if(gradeWeight != null){
                    if(gradeWeight.getReach() != null && gradeWeight.getReach().compareTo(new BigDecimal(0)) >= 1){
                        if(rate.compareTo(gradeWeight.getReach()) > -1){
                            fluxDailyRate.setFlag(true);
                        }
                    }
                }
            }
        }
        this.fluxDailyRateService.save(fluxDailyRate);
    }


}
