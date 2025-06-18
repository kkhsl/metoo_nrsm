package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.freemarker.FreemarkerUtil;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.FlowStatistics;
import com.metoo.nrsm.entity.GradeWeight;
import com.metoo.nrsm.entity.NetworkElement;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@RequestMapping("admin/analysis")
@Controller
public class FluxFreemakeManagerController {

    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private IGradWeightService gradWeightService;
    @Autowired
    private FreemarkerUtil freemarkerUtil;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    public static void main(String[] args) {
        BigDecimal a = new BigDecimal(0);
        BigDecimal b = new BigDecimal(0);
        if (!a.equals(BigDecimal.ZERO) || !b.equals(BigDecimal.ZERO)) {
            System.out.println(true);
        }

        System.out.println(new BigDecimal(0.1).compareTo(new BigDecimal(0)));


        BigDecimal c = new BigDecimal(4);

        BigDecimal d = new BigDecimal(4);

        BigDecimal e = new BigDecimal(3);

        System.out.println(c.add(d.divide(e, 2, BigDecimal.ROUND_HALF_UP)));


        int n = 2;

        int num = 3;

        n += num;

        System.out.println(n);
    }

    @RequestMapping
    public String showHtml(Model model) {
        this.getData(model);
        return "analysis";
    }

    public List deviceCount() {

        List list = new ArrayList();

        List<DeviceType> deviceTypeList = this.deviceTypeService.selectCountByJoin();
        DeviceType deviceType = new DeviceType();
        deviceType.setName("网元总数");
        if (deviceTypeList.size() > 0) {
            int j = 0;
            for (DeviceType obj : deviceTypeList) {
                int n = 0;
                if (obj.getNetworkElementList().size() > 0) {
                    for (NetworkElement ne : deviceType.getNetworkElementList()) {
                        // 设备是否在线
                        int num = onlineDevice(ne);
                        n += num;
                        j += num;
                    }
                    deviceType.getNetworkElementList().clear();
                    deviceType.setOnlineCount(n);
                }
                list.add(deviceType);
            }
            int count = deviceTypeList.stream().mapToInt(e -> e.getCount()).sum();
            deviceType.setCount(count);
            deviceType.setOnlineCount(j);
        }
        list.add(0, deviceType);
        return list;
    }

    public int onlineDevice(NetworkElement networkElement) {
        if (networkElement.getIp() != null) {
            // snmp状态
            String path = Global.PYPATH + "gethostname.py";
            String[] params = {networkElement.getIp(), networkElement.getVersion(),
                    networkElement.getCommunity()};
            String hostname = pythonExecUtils.exec(path, params);
            if (StringUtils.isNotEmpty(hostname)) {
                return 1;
            }
        }
        return 0;
    }

    public List terminalCount() {

        List<DeviceType> list = new ArrayList();

        DeviceType terminal = new DeviceType();
        terminal.setName("终端总数");
        List<DeviceType> terminals = this.deviceTypeService.selectTerminalCountByJoin();
        if (terminals.size() > 0) {
            for (DeviceType deviceType : terminals) {
                int n = deviceType.getTerminalList().stream().mapToInt(e -> e.getOnline() == true ? 1 : 0).sum();
                deviceType.setOnlineCount(n);
                deviceType.getTerminalList().clear();
                list.add(deviceType);
            }
            int count = terminals.stream().mapToInt(e -> e.getCount()).sum();
            terminal.setCount(count);
            int onlineCount = list.stream().mapToInt(e -> e.getOnlineCount()).sum();
            terminal.setOnlineCount(onlineCount);
        }
        return list;
    }

    public void getData(Model model) {
        model.addAttribute("currentTime", DateTools.getCurrentDate(new Date(), "yyyy-MM-dd HH:mm:ss"));

        model.addAttribute("ne", JSONObject.toJSONString(this.deviceCount()));

        model.addAttribute("terminal", JSONObject.toJSONString(this.terminalCount()));

        model.addAttribute("currentTime", DateTools.getCurrentDate(new Date(), "yyyy-MM-dd HH:mm:ss"));

        // 网元总数
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        model.addAttribute("networkElements", networkElements);

        // 设备饼图
        Map params = new HashMap();
        params.put("isipv6", true);
        List<NetworkElement> v4ip_v6ip_count = networkElementService.selectObjByMap(params);

        params.clear();
        params.put("isipv6", false);
        List<NetworkElement> ipv4_count = networkElementService.selectObjByMap(params);

        List neStatistics = new ArrayList();

        Map ipv4Map = new HashMap();
        ipv4Map.put("name", "Ipv4设备");
        ipv4Map.put("count", ipv4_count.size());

        Map ipv6Map = new HashMap();
        ipv6Map.put("name", "Ipv4/Ipv6设备");
        ipv6Map.put("count", v4ip_v6ip_count.size());
        neStatistics.add(ipv4Map);
        neStatistics.add(ipv6Map);

        model.addAttribute("neStatistics", JSONObject.toJSONString(neStatistics));

        // 终端饼图
        List terminalStatistics = new ArrayList();
        Map<String, Integer> terminalCount = this.terminalService.terminalCount();
        if (terminalCount != null) {
            Map terminalIpv4Map = new HashMap();
            terminalIpv4Map.put("name", "Ipv4终端");
            terminalIpv4Map.put("count", terminalCount.get("v4ip_count"));
            Map terminalIpv6Map = new HashMap();
            terminalIpv6Map.put("name", "Ipv6终端");
            terminalIpv6Map.put("count", terminalCount.get("v6ip_count"));
            Map terminalIpv4AndIpv6Map = new HashMap();
            terminalIpv4AndIpv6Map.put("name", "Ipv4/Ipv6终端");
            terminalIpv4AndIpv6Map.put("count", terminalCount.get("v4ip_v6ip_count"));
            terminalStatistics.add(terminalIpv4Map);
            terminalStatistics.add(terminalIpv6Map);
            terminalStatistics.add(terminalIpv4AndIpv6Map);


            model.addAttribute("terminalStatistics", JSONObject.toJSONString(terminalStatistics));

            // 流量统计
            params.clear();
            params.put("startOfDay", DateTools.getStartOfDay());
            params.put("endOfDay", DateTools.getEndOfDay());
            List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
            model.addAttribute("flowStatisticsList",
                    JSONObject.toJSONStringWithDateFormat(flowStatisticsList, "yyyy-MM-dd HH:mm:ss"));

            // 评分
            GradeWeight gradeWeight = this.gradWeightService.selectObjOne();

            // 设备比例
            BigDecimal neRatio = this.neRatio(ipv4_count.size(), v4ip_v6ip_count.size());
            if (gradeWeight != null) {
                neRatio = neRatio.multiply(gradeWeight.getNe());
                // 终端比例
                BigDecimal terminalRatio = this.terminalRatio(terminalCount);
                terminalRatio = terminalRatio.multiply(gradeWeight.getTerminal());
                // 流量比例
                BigDecimal fluxRatioRatio = fluxRatio(flowStatisticsList);
                fluxRatioRatio = fluxRatioRatio.multiply(gradeWeight.getFlux());

                BigDecimal grade = neRatio.add(terminalRatio).add(fluxRatioRatio).divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);

//        grade = grade.setScale(0, BigDecimal.ROUND_HALF_UP);

                model.addAttribute("grade", grade.multiply(new BigDecimal("100")));

                if (gradeWeight.getReach() != null && gradeWeight.getReach().compareTo(new BigDecimal(0)) >= 1) {
                    if (grade.compareTo(gradeWeight.getReach()) > -1) {
                        model.addAttribute("grade", 100);
                    }
                }
            }
        }
    }

    public BigDecimal neRatio(Integer v4ip_count, Integer v4ip_v6ip_count) {

        BigDecimal v4ip_count_bigDecimal = new BigDecimal(v4ip_count);
        BigDecimal v4ip_v6ip_count_bigdecimal = new BigDecimal(v4ip_v6ip_count);

        BigDecimal sum_bigDecimal = v4ip_count_bigDecimal.add(v4ip_v6ip_count_bigdecimal);

        BigDecimal a = v4ip_count_bigDecimal.divide(sum_bigDecimal, 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal b = v4ip_v6ip_count_bigdecimal.divide(sum_bigDecimal, 2, BigDecimal.ROUND_HALF_UP);

        return b;
    }

    public BigDecimal terminalRatio(Map<String, Integer> terminalCount) {
        Integer v4ip_count = terminalCount.get("v4ip_count");
        Integer v6ip_count = terminalCount.get("v6ip_count");
        Integer v4ipv6ip_count = terminalCount.get("v4ip_v6ip_count");

        BigDecimal v4ip_count_bigDecimal = new BigDecimal(v4ip_count);
        BigDecimal v6ip_countbig_decimal = new BigDecimal(v6ip_count);
        BigDecimal v4ipv6ip_count_bigDecimal = new BigDecimal(v4ipv6ip_count);

        BigDecimal sum_bigDecimal = v4ip_count_bigDecimal.add(v6ip_countbig_decimal).add(v4ipv6ip_count_bigDecimal);

        BigDecimal a = v4ip_count_bigDecimal.divide(sum_bigDecimal, 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal b = v6ip_countbig_decimal.divide(sum_bigDecimal, 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal c = v4ipv6ip_count_bigDecimal.divide(sum_bigDecimal, 2, BigDecimal.ROUND_HALF_UP);

        return b.add(c);
    }

    public BigDecimal fluxRatio(List<FlowStatistics> flowStatisticsList) {
//        int sum2 = INNERLIST.stream().mapToInt(Innser -> Innser.getAge()).sum();
//.filter(Objects::nonNull)

        BigDecimal sum1 = flowStatisticsList.stream().map(e -> e.getIpv6Rate() != null ? e.getIpv6Rate() : new BigDecimal(0)
        ).collect(Collectors.toList()).stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sum = flowStatisticsList.stream().filter(e -> e.getIpv6Rate() != null).map(FlowStatistics::getIpv6Rate)
                .collect(Collectors.toList())
                .stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        sum = sum.divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);
        return sum;
    }

    @Test
    public void test() {
        BigDecimal originalNumber = new BigDecimal("10.500"); // 原始BigDecimal对象
        BigDecimal strippedNumber = originalNumber.setScale(0, BigDecimal.ROUND_DOWN);

// 输出结果
        System.out.println("去除小数位后的BigDecimal对象：" + strippedNumber);
    }

    @Test
    public void optionalTest() {
        Integer value1 = null;
        Integer value2 = 10;
        // 允许传递为null参数
        Optional<Integer> a = Optional.ofNullable(value1);
        // 如果传递的参数是null，抛出异常NullPointerException
        Optional<Integer> b = Optional.of(value2);
        // 空对象，value为null
        Optional<Object> c = Optional.empty();

        System.out.println(a.isPresent());
        System.out.println(b.get());
        System.out.println(c);

        Integer d = null;

    }

    @RequestMapping("/createHtml")
    public void createHtml(HttpServletResponse response) throws Exception {
        Map data = this.getData();
        response.setContentType("application/html; charset=UTF-8");
        response.setHeader("Content-Disposition", "Attachment;filename= " + new String(("analyse" + DateTools.getCurrentDate(new Date(), "yyyyMMddHHmm") + ".html").getBytes("UTF-8"), "UTF-8"));
        this.freemarkerUtil.createHtmlToBrowser("analysis.ftl", data, response.getWriter());
    }

    public Map getData() {
        Map obj = new HashMap();
        obj.put("currentTime", DateTools.getCurrentDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        List<DeviceType> nes = this.deviceTypeService.selectCountByJoin();
        List list1 = new ArrayList();
        DeviceType network = new DeviceType();
        network.setName("网元总数");
        if (nes.size() > 0) {
            int j = 0;
            for (DeviceType deviceType : nes) {
                int n = 0;
                if (deviceType.getNetworkElementList().size() > 0) {
                    for (NetworkElement ne : deviceType.getNetworkElementList()) {
                    }
                    deviceType.getNetworkElementList().clear();
                    deviceType.setOnlineCount(n);
                }
                list1.add(deviceType);
            }
            int count = nes.stream().mapToInt(e -> e.getCount()).sum();
            network.setCount(count);
            network.setOnlineCount(j);
        }
        list1.add(0, network);
        obj.put("ne", JSONObject.toJSONString(list1));
        List<DeviceType> list2 = new ArrayList();
        DeviceType terminal = new DeviceType();
        terminal.setName("终端总数");
        List<DeviceType> terminals = this.deviceTypeService.selectTerminalCountByJoin();
        if (terminals.size() > 0) {
            for (DeviceType deviceType : terminals) {
                int n = deviceType.getTerminalList().stream().mapToInt(e -> e.getOnline() == true ? 1 : 0).sum();
                deviceType.setOnlineCount(n);
                deviceType.getTerminalList().clear();
                list2.add(deviceType);
            }
            int count = terminals.stream().mapToInt(e -> e.getCount()).sum();
            terminal.setCount(count);
            int onlineCount = list2.stream().mapToInt(e -> e.getOnlineCount()).sum();
            terminal.setOnlineCount(onlineCount);
        }
        list2.add(0, terminal);
        obj.put("terminal", JSONObject.toJSONString(list2));

        obj.put("currentTime", DateTools.getCurrentDate(new Date(), "yyyy-MM-dd HH:mm:ss"));

        // 网元总数
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        obj.put("networkElements", networkElements);

        // 设备数量
        Map params = new HashMap();
        params.put("isipv6", true);
        List<NetworkElement> v4ip_v6ip_count = networkElementService.selectObjByMap(params);
        params.clear();
        params.put("isipv6", false);
        List<NetworkElement> ipv4_count = networkElementService.selectObjByMap(params);

        List neStatistics = new ArrayList();
        Map ipv4Map = new HashMap();
        ipv4Map.put("name", "Ipv4设备");
        ipv4Map.put("count", ipv4_count.size());

        Map ipv6Map = new HashMap();
        ipv6Map.put("name", "Ipv4/Ipv6设备");
        ipv6Map.put("count", v4ip_v6ip_count.size());
        neStatistics.add(ipv4Map);
        neStatistics.add(ipv6Map);
        obj.put("neStatistics", JSONObject.toJSONString(neStatistics));


        List terminalStatistics = new ArrayList();
        Map<String, Integer> terminalCount = this.terminalService.terminalCount();
        Map terminalIpv4Map = new HashMap();
        terminalIpv4Map.put("name", "Ipv4终端");
        terminalIpv4Map.put("count", terminalCount.get("v4ip_count"));
        Map terminalIpv6Map = new HashMap();
        terminalIpv6Map.put("name", "Ipv6终端");
        terminalIpv6Map.put("count", terminalCount.get("v6ip_count"));
        Map terminalIpv4AndIpv6Map = new HashMap();
        terminalIpv4AndIpv6Map.put("name", "Ipv4/Ipv6终端");
        terminalIpv4AndIpv6Map.put("count", terminalCount.get("v4ip_v6ip_count"));
        terminalStatistics.add(terminalIpv4Map);
        terminalStatistics.add(terminalIpv6Map);
        terminalStatistics.add(terminalIpv4AndIpv6Map);
        obj.put("terminalStatistics", JSONObject.toJSONString(terminalStatistics));

        params.clear();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", DateTools.getEndOfDay());
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        obj.put("flowStatisticsList", JSONObject.toJSONStringWithDateFormat(flowStatisticsList, "yyyy-MM-dd HH:mm:ss"));

        // 评分
        GradeWeight gradeWeight = this.gradWeightService.selectObjOne();
        // 设备比例
        BigDecimal neRatio = this.neRatio(ipv4_count.size(), v4ip_v6ip_count.size());
        neRatio = neRatio.multiply(gradeWeight.getNe());
        // 终端比例
        BigDecimal terminalRatio = this.terminalRatio(terminalCount);
        terminalRatio = terminalRatio.multiply(gradeWeight.getTerminal());
        // 流量比例
        BigDecimal fluxRatioRatio = fluxRatio(flowStatisticsList);
        fluxRatioRatio = fluxRatioRatio.multiply(gradeWeight.getFlux());

        BigDecimal grade = neRatio.add(terminalRatio).add(fluxRatioRatio).divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
        obj.put("grade", grade.setScale(0, BigDecimal.ROUND_HALF_UP));
        return obj;
    }


}
