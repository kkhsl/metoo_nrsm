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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    @GetMapping("/scanByTerminal")
    public void scanByTerminal(){
        probeService.scanByTerminal();
    }


    @GetMapping("/gatherAll")
    public void gather(){
        dhcpService.gather(DateTools.gatherDate());
        dhcp6Service.gather(DateTools.gatherDate());
        gatherService.gatherIpv4Detail(DateTools.gatherDate());
        gatherService.gatherPort(DateTools.gatherDate());
        gatherService.gatherPortIpv6(DateTools.gatherDate());
        gatherService.gatherIpv4(DateTools.gatherDate());
        gatherService.gatherIpv6(DateTools.gatherDate());
        gatherService.gatherArp(DateTools.gatherDate());
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
