package com.metoo.nrsm.core.traffic.gather;

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
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Component
public class TrafficFactoryMainImpl implements Gather {

    @Override
    public void executeMethod() {
        log.info("Traffic start=========");
        IFlowUnitService flowUnitService = (IFlowUnitService) ApplicationContextUtils.getBean("flowUnitServiceImpl");
        IGatewayService gatewayService = (IGatewayService) ApplicationContextUtils.getBean("gatewayServiceImpl");
        PyExecUtils pyExecUtils = (PyExecUtils) ApplicationContextUtils.getBean("pyExecUtils");
        List<Gateway> list = gatewayService.selectObjByMap(null);

        if (list.size() > 0) {
            for (Gateway gateway : list) {
                Date date = new Date();
                try {
                    if (gateway != null) {

                        Map params = new HashMap();
                        params.put("hidden", false);
                        params.put("gatewayId", gateway.getId());
                        List<FlowUnit> units = flowUnitService.selectObjByMap(params);
                        if (units.size() <= 0) {
                            return;
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
                                "vlan_policy"});

                        String result = pyExecUtils.exec(pyCommand);

                        log.info("Traffic result: ================= {} ", result);

                        if (StringUtil.isNotEmpty(result)) {
                            for (FlowUnit unit : units) {
                                try {
                                    processFlowUnit(unit, result);
                                    unit.setAddTime(date);
                                    flowUnitService.update(unit);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    unit.setVfourFlow("0");
                                    unit.setVsixFlow("0");
                                    flowUnitService.update(unit);
                                }
                            }
                        } else {
                            for (FlowUnit unit : units) {
                                // 清空单位流量
                                unit.setAddTime(date);
                                unit.setVfourFlow("0");
                                unit.setVsixFlow("0");
                                flowUnitService.update(unit);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("Traffic end =========");
        }
    }


    /**
     * 处理单个 FlowUnit，根据当前流量和历史流量计算流量增量
     * @param unit FlowUnit 对象
     * @param jsonData 当前采集的流量数据
     */
    public static void processFlowUnit(FlowUnit unit, String jsonData) {
        // 获取当前 VLAN 的流量数据
        JSONObject currentFlow = TrafficAnalyzerUtilsBack.getVlanTrafficStats(jsonData, unit.getVlanNum());
        if (currentFlow == null) {
            // 如果当前流量数据为空，则将流量设置为0，并返回
            unit.setVsixFlow("0");
            unit.setVfourFlow("0");
            unit.setVfourFlowTotal("0");
            unit.setVsixFlowTotal("0");
            return;
        }

        // 获取历史总流量数据（如果存在）
        String vfourFlowTotal = unit.getVfourFlowTotal(); // 总流量 (IPv4)
        String vsixFlowTotal = unit.getVsixFlowTotal();   // 总流量 (IPv6)

        // 从当前流量中获取 IPv4 和 IPv6 流量
        double currentIpv4Traffic = currentFlow.getDouble("IPv4Traffic");
        double currentIpv6Traffic = currentFlow.getDouble("IPv6Traffic");

        // 初始化差值流量
        double ipv4TrafficDiff = 0;
        double ipv6TrafficDiff = 0;

        // 计算增量流量（当前流量 - 上次的总流量）
        if (vfourFlowTotal != null && !vfourFlowTotal.isEmpty()) {
            double lastIpv4FlowTotal = Double.parseDouble(vfourFlowTotal);
            ipv4TrafficDiff = currentIpv4Traffic - lastIpv4FlowTotal;
        }

        if (vsixFlowTotal != null && !vsixFlowTotal.isEmpty()) {
            double lastIpv6FlowTotal = Double.parseDouble(vsixFlowTotal);
            ipv6TrafficDiff = currentIpv6Traffic - lastIpv6FlowTotal;
        }

        // 创建 DecimalFormat 来保留两位小数
        DecimalFormat df = new DecimalFormat("0.00");

        // 设置增量流量，保留两位小数
        if(ipv4TrafficDiff != 0){
            unit.setVfourFlow(df.format(ipv4TrafficDiff));  // 设置增量流量
        }
        if(ipv6TrafficDiff != 0){
            unit.setVsixFlow(df.format(ipv6TrafficDiff));   // 设置增量流量
        }
        // 更新总流量为当前流量，保留两位小数
        if(currentIpv4Traffic != 0){
            unit.setVfourFlowTotal(df.format(currentIpv4Traffic));  // 更新 IPv4 总流量
        }
        if(currentIpv6Traffic != 0){
            unit.setVsixFlowTotal(df.format(currentIpv6Traffic));   // 更新 IPv6 总流量
        }
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

}
