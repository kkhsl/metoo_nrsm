package com.metoo.nrsm.core.config.utils.gather.strategy.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.strategy.Context;
import com.metoo.nrsm.core.config.utils.gather.strategy.DataCollectionStrategy;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.ITrafficService;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Traffic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 14:49
 * <p>
 * ipv4 port 采集
 */
@Slf4j
@Component
public class TrafficByGatewayCollectionStrategy implements DataCollectionStrategy {

    private final IFlowUnitService flowUnitService;
    private final IGatewayService gatewayService;
    private final ITrafficService trafficService;
    private final PyExecUtils pyExecUtils;

    @Autowired
    public TrafficByGatewayCollectionStrategy(IFlowUnitService flowUnitService, IGatewayService gatewayService, ITrafficService trafficService,
                                              PyExecUtils pyExecUtils) {
        this.flowUnitService = flowUnitService;
        this.gatewayService = gatewayService;
        this.trafficService = trafficService;
        this.pyExecUtils = pyExecUtils;
    }

    @Override
    public void collectData(Context context) {
        try {
            Gateway gateway = (Gateway) context.getEntity();
            Date date = context.getAddTime();
            if (gateway != null) {

                String vlanNum = "";
                String pattern = "";

                Map params = new HashMap();
                params.put("hidden", false);
                params.put("gatewayId", gateway.getId());
                List<FlowUnit> units = flowUnitService.selectObjByMap(params);
                if (units.size() <= 0) {
                    return;
                } else {
                    FlowUnit unit = units.get(0);
                    vlanNum = unit.getVlanNum();
                    pattern = unit.getPattern();
                }

                PyCommandBuilder3 pyCommand = new PyCommandBuilder3();
                pyCommand.setVersion(Global.py_name);
                pyCommand.setPath(Global.py_path);
                pyCommand.setPy_prefix("-W ignore");
                pyCommand.setName("controller.py");
                pyCommand.setParams(new String[]{
                        gateway.getVendorAlias(),
                        gateway.getIp(),
                        gateway.getLoginType(),
                        gateway.getLoginPort(),
                        gateway.getLoginName(),
                        gateway.getLoginPassword(),
                        vlanNum, pattern});

                String result = this.pyExecUtils.exec(pyCommand);
                if (StringUtil.isNotEmpty(result)) {
                    for (FlowUnit unit : units) {
                        try {
                            // 根据pattern，判断使用哪种方式获取流量
                            if (pattern.equals("1")) {

                                this.insertTraffic2(result, unit, date);

                            } else if (pattern.equals("0")) {
                                log.info("controller ============== pattern 0 =============== ");

                                log.info("controller - data " + result);

                                this.insertTrafficYingTan(result, unit, date);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        flowUnitService.update(unit);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertTraffic2(String data, FlowUnit unit, Date date) {
        if (StringUtil.isNotEmpty(data)) {
            JSONArray jsonArray = JSONArray.parseArray(data);
            if (jsonArray.size() > 0) {

                double ipv4Inbound = 0;

                double ipv4Outbound = 0;

                double ipv6Inbound = 0;

                double ipv6Outbound = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.get("Protocol") != null && jsonObject.get("Protocol").equals("Ipv4")) {
                        if (jsonObject.containsKey("Input")) {
                            ipv4Inbound += Double.parseDouble(jsonObject.getString("Input"));
                        }
                        if (jsonObject.containsKey("Output")) {
                            ipv4Outbound += Double.parseDouble(jsonObject.getString("Output"));
                        }
                    }

                    if (jsonObject.get("Protocol") != null && jsonObject.get("Protocol").equals("Ipv6")) {
                        if (jsonObject.containsKey("Input")) {
                            ipv6Inbound += Double.parseDouble(jsonObject.getString("Input"));
                        }
                        if (jsonObject.containsKey("Output")) {
                            ipv6Outbound += Double.parseDouble(jsonObject.getString("Output"));
                        }
                    }
                }


                double vfourFlow = (ipv4Inbound + ipv4Outbound) / 1000000;

                DecimalFormat df = new DecimalFormat("#.##");
                String formattedVfourFlow = df.format(vfourFlow);

                double vsixFlow = (ipv6Inbound + ipv6Outbound) / 1000000;
                String formattedVsixFlow = df.format(vsixFlow * 10);

                unit.setVfourFlow(formattedVfourFlow);
                unit.setVsixFlow(formattedVsixFlow);

                // 入库traffic表
                try {
                    Traffic traffic = new Traffic();
                    traffic.setAddTime(date);
                    traffic.setVfourFlow(formattedVfourFlow);
                    traffic.setVsixFlow(formattedVsixFlow);
                    trafficService.save(traffic);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void insertTrafficYingTan(String data, FlowUnit unit, Date date) {

        log.info("controller - data - start ==========================");
        if (StringUtil.isNotEmpty(data)) {

            JSONArray jsonArray = JSONArray.parseArray(data);
            String rule = unit.getRule();
            if (jsonArray.size() > 0) {

                double ipv4Inbound1 = 0;

                double ipv4Outbound1 = 0;
                double ipv4Outbound2 = 0;

                double ipv6Inbound1 = 0;

                double ipv6Outbound1 = 0;
                double ipv6Outbound2 = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-in")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    if (StringUtil.isNotEmpty(nestedObject.getString(rule))) {
                                        ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    }
                                }
                            }
                        }
                    }

                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {


                                    if (StringUtil.isNotEmpty(nestedObject.getString(rule))) {

                                        ipv4Outbound1 += Double.parseDouble(nestedObject.getString(rule));
                                        ipv4Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                    }

//                                    ipv4Outbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                    ipv4Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }


                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {

//                                    ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));

                                    if (StringUtil.isNotEmpty(nestedObject.getString(rule))) {

                                        ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    }

                                }
                            }
                        }
                    }


                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {

//                                    ipv6Outbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                    ipv6Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));

                                    if (StringUtil.isNotEmpty(nestedObject.getString(rule))) {

                                        ipv6Outbound1 += Double.parseDouble(nestedObject.getString(rule));
                                        ipv6Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                    }
                                }
                            }
                        }
                    }

                }
                System.out.println("ipv4Inbound1: " + ipv4Inbound1);

                double ipv4Inbound = (ipv4Inbound1 / 2) * 5 / 1000000;


                double ipv4Outbound = ipv4Outbound1 - ipv4Outbound2;
                ipv4Outbound = (ipv4Outbound / 2) * 5 / 1000000;

                double vfourFlow = 0.01;

                if ((ipv4Outbound < 0.01 && ipv4Outbound > 0) && (ipv4Inbound < 0.01 && ipv4Inbound > 0)) {
                    vfourFlow = 0.01;
                } else {
                    if (ipv4Outbound < 0.01 && ipv4Outbound > 0) {
                        ipv4Outbound = 0.01;
                    }
                    if (ipv4Inbound < 0.01 && ipv4Inbound > 0) {
                        ipv4Inbound = 0.01;
                    }
                    vfourFlow = ipv4Inbound + ipv4Outbound;
                }


                double ipv6Inbound = (ipv6Inbound1 / 2) * 5 / 1000000;

                double ipv6Outbound = ipv6Outbound1 - ipv6Outbound2;

                ipv6Outbound = (ipv6Outbound / 2) * 5 / 1000000;

                double vsixFlow = 0.01;

                if ((ipv6Outbound < 0.01 && ipv6Outbound > 0) && (ipv6Inbound < 0.01 && ipv6Inbound > 0)) {
                    vsixFlow = 0.01;
                } else {
                    if (ipv6Outbound < 0.01 && ipv6Outbound > 0) {
                        ipv6Outbound = 0.01;
                    }
                    if (ipv6Inbound < 0.01 && ipv6Inbound > 0) {
                        ipv6Inbound = 0.01;
                    }
                    vsixFlow = ipv6Inbound + ipv6Outbound;
                }

                DecimalFormat df = new DecimalFormat("#.##");

                String formattedVfourFlow = df.format(vfourFlow);

                String formattedVsixFlow = df.format(vsixFlow * 10);

                unit.setVfourFlow(formattedVfourFlow);
                unit.setVsixFlow(formattedVsixFlow);

                log.info("controller - data - start - vfourFlow ==========================" + vfourFlow);

                log.info("controller - data - start - vsixFlow ==========================" + vsixFlow);

                log.info("controller - data - start - ipv6Inbound ==========================" + ipv6Inbound);

                log.info("controller - data - start - ipv6Outbound ==========================" + ipv6Outbound);

                log.info("controller - data - end - formattedVfourFlow  ==========================" + formattedVfourFlow);

                log.info("controller - data - end - formattedVsixFlow  ==========================" + formattedVsixFlow);

                // 入库traffic表
                try {
                    log.info("controller=================================start");
                    Traffic traffic = new Traffic();
                    traffic.setAddTime(date);
                    traffic.setVfourFlow(formattedVfourFlow);
                    traffic.setVsixFlow(formattedVsixFlow);
                    traffic.setUnitName(unit.getUnitName());
                    int i = trafficService.save(traffic);


                    log.info("controller=================================end" + i + "num");
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                return ipv4Inbound1 - ipv4Inbound2;
            }
        }
//        return 0 ;
    }
}
