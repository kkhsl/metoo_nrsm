package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.utils.TrafficAnalyzerUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.utils.TrafficAnalyzerUtilsBack;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.manager.utils.AESUtils;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Gateway;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Component
public class TrafficFactoryGanzhouImpl implements Gather {

    @Override
    public void executeMethod() {
        log.info("Traffic start=========");
        IFlowUnitService flowUnitService = (IFlowUnitService) ApplicationContextUtils.getBean("flowUnitServiceImpl");
        IGatewayService gatewayService = (IGatewayService) ApplicationContextUtils.getBean("gatewayServiceImpl");
        PyExecUtils pyExecUtils = (PyExecUtils) ApplicationContextUtils.getBean("pyExecUtils");

        Date date = new Date();
        Map params = new HashMap();
        params.put("hidden", false);
        List<FlowUnit> units = flowUnitService.selectObjByMap(params);
        if (units.size() <= 0) {
            return;
        } else {
            try {
                for (FlowUnit unit : units) {
                    params.clear();
                    params.put("hidden", false);
                    params.put("id", unit.getGatewayId());
                    List<Gateway> list = gatewayService.selectObjByMap(params);
                    Gateway gateway = null;
                    if(list.size() > 0){
                        gateway = list.get(0);
                    }

                    PyCommandBuilder3 pyCommand = new PyCommandBuilder3();
                    pyCommand.setVersion(Global.py_name);
                    pyCommand.setPath(Global.py_path);
                    pyCommand.setPy_prefix("-W ignore");
                    pyCommand.setName("main.py");
                    pyCommand.setParams(new String[]{"switch",
                            gateway.getVendorAlias(),
                            gateway.getIp(),
                            gateway.getLoginType(),
                            gateway.getLoginPort(),
                            gateway.getLoginName(),
                            AESUtils.decrypt(gateway.getLoginPassword()),
                            "policy_s5720_in"});

                    String traffic_in = pyExecUtils.exec(pyCommand);
                    //[{'direction': 'inbound', 'ipv4bytes': '38,712,801,668', 'ipv6bytes': '218,796,447'}]

                    PyCommandBuilder3 pyCommand_out = new PyCommandBuilder3();
                    pyCommand_out.setVersion(Global.py_name);
                    pyCommand_out.setPath(Global.py_path);
                    pyCommand_out.setPy_prefix("-W ignore");
                    pyCommand_out.setName("main.py");
                    pyCommand_out.setParams(new String[]{"switch",
                            gateway.getVendorAlias(),
                            gateway.getIp(),
                            gateway.getLoginType(),
                            gateway.getLoginPort(),
                            gateway.getLoginName(),
                            AESUtils.decrypt(gateway.getLoginPassword()),
                            "policy_s5720_out"});

                    String traffic_out = pyExecUtils.exec(pyCommand_out);
                    // [{'direction': 'outbound', 'ipv4bytes': '38,898,076,968', 'ipv6bytes': '392,763,584'}]

                    if (StringUtil.isNotEmpty(traffic_in) || StringUtil.isNotEmpty(traffic_out)) {
                        try {
                            processFlowUnit(unit, traffic_in, traffic_out);
                            unit.setAddTime(date);
                            flowUnitService.update(unit);
                        } catch (Exception e) {
                            e.printStackTrace();
                            unit.setVfourFlow("0");
                            unit.setVsixFlow("0");
                            flowUnitService.update(unit);
                        }
                    } else {
                        for (FlowUnit flowUnit : units) {
                            // 清空单位流量
                            flowUnit.setAddTime(date);
                            flowUnit.setVfourFlow("0");
                            flowUnit.setVsixFlow("0");
                            flowUnitService.update(flowUnit);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

//    [{'direction': 'inbound', 'ipv4bytes': '38,712,801,668', 'ipv6bytes': '218,796,447'}]
//     [{'direction': 'outbound', 'ipv4bytes': '38,898,076,968', 'ipv6bytes': '392,763,584'}]
    public static void processFlowUnit(FlowUnit unit, String trafficIn, String trafficOut) {
        // 1. 解析入向和出向流量数据
        JSONArray inFlowData = new JSONArray(trafficIn);
        JSONArray outFlowData = new JSONArray(trafficOut);

        // 2. 获取入向和出向流量（IPv4 + IPv6）
        JSONObject inFlow = inFlowData.length() > 0 ? inFlowData.getJSONObject(0) : new JSONObject();
        JSONObject outFlow = outFlowData.length() > 0 ? outFlowData.getJSONObject(0) : new JSONObject();

        // 3. 解析流量（移除逗号，转为 double）
        double inIpv4 = parseTrafficValue(inFlow.optString("ipv4bytes", "0"));
        double inIpv6 = parseTrafficValue(inFlow.optString("ipv6bytes", "0"));
        double outIpv4 = parseTrafficValue(outFlow.optString("ipv4bytes", "0"));
        double outIpv6 = parseTrafficValue(outFlow.optString("ipv6bytes", "0"));

        // 4. 计算总流量（Bytes）
        double currentIpv4Bytes = inIpv4 + outIpv4;
        double currentIpv6Bytes = inIpv6 + outIpv6;

        // 5. 转换为 MB（真实流量）
        double currentIpv4MB = currentIpv4Bytes / 1000000.0;
        double currentIpv6MB = currentIpv6Bytes / 1000000.0;

        // 6. 获取历史总流量（MB）
        String vfourFlowTotalMB = unit.getVfourFlowTotal(); // 历史 IPv4 总流量（MB）
        String vsixFlowTotalMB = unit.getVsixFlowTotal();   // 历史 IPv6 总流量（MB）

        // 7. 计算增量流量（当前 - 历史）
        double ipv4TrafficDiffMB = 0;
        double ipv6TrafficDiffMB = 0;

        if (vfourFlowTotalMB != null && !vfourFlowTotalMB.isEmpty()) {
            double lastIpv4MB = Double.parseDouble(vfourFlowTotalMB);
            ipv4TrafficDiffMB = currentIpv4MB - lastIpv4MB;
        }

        if (vsixFlowTotalMB != null && !vsixFlowTotalMB.isEmpty()) {
            double lastIpv6MB = Double.parseDouble(vsixFlowTotalMB);
            ipv6TrafficDiffMB = currentIpv6MB - lastIpv6MB;
        }

        // 8. 格式化（保留 2 位小数）
        DecimalFormat df = new DecimalFormat("0.00");

        // 9. 更新 FlowUnit
        unit.setVfourFlow(df.format(ipv4TrafficDiffMB));       // IPv4 增量流量（MB）
        unit.setVsixFlow(df.format(ipv6TrafficDiffMB));       // IPv6 增量流量（MB）
        unit.setVfourFlowTotal(df.format(currentIpv4MB));      // IPv4 总流量（MB）
        unit.setVsixFlowTotal(df.format(currentIpv6MB));      // IPv6 总流量（MB）
    }

    /**
     * 辅助方法：解析流量值（移除逗号，转为 double）
     */
    private static double parseTrafficValue(String trafficStr) {
        if (trafficStr == null || trafficStr.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(trafficStr.replace(",", ""));
    }

    /**
     * 存储流量数据到数据库
     * @param flowData 要存储的流量数据
     */
    public static void saveTrafficData(JSONObject flowData) {
        // 在此处实现数据库存储逻辑
        // 比如通过 JDBC 或 ORM 存储到数据库
        System.out.println("Saving to database: " + flowData.toString());
    }

    private static String getTrafficAll(String result) {
        // 1. 解析新数据
        String dataAll = TrafficAnalyzerUtils.analyzer(result);
        return dataAll;
    }


//    // 2. 获取历史数据
//    String historyFlow = unit.getFlowHistory();
//
//    // 3. 计算差值（新数据 - 历史数据）
//    String flowDifference = TrafficAnalyzerUtils.calculateFlowDifference(newFlow, historyFlow);
//
//        log.info("差值：{}", flowDifference);

    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("0.00");
        double ipv4TrafficDiff = 0;
        String a = df.format(ipv4TrafficDiff);
        System.out.println(a);

    }

}
