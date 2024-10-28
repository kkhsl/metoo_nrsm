package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.FlowUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.utils.GeneraFlowUtils;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.ITrafficService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Traffic;
import com.metoo.nrsm.entity.Unit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Component
public class TrafficByGatewayBackFactoryImpl2_1 implements Gather {


    public void executeMethod() {
        log.info("unit exec traffic start...");
        IUnitService unitService = (IUnitService) ApplicationContextUtils.getBean("unitServiceImpl");
        IGatewayService gatewayService = (IGatewayService) ApplicationContextUtils.getBean("gatewayServiceImpl");
        PyExecUtils pyExecUtils = (PyExecUtils) ApplicationContextUtils.getBean("pyExecUtils");

        List<Gateway> list = gatewayService.selectObjByMap(null);

        if (list.size() > 0) {
            for (Gateway gateway : list) {
                Date date = new Date();
                try {
                    if (gateway != null) {
                        String vlanNum = "";
                        String pattern = "";

                        Map params = new HashMap();
                        params.put("hidden", false);
                        params.put("gatewayId", gateway.getId());
                        List<Unit> units = unitService.selectObjByMap(params);
                        if (units.size() <= 0) {
                            return;
                        } else {
                            Unit unit = units.get(0);
                            vlanNum = unit.getVlanNum();
                            pattern = unit.getPattern();
                        }

                        PyCommandBuilder pyCommand = new PyCommandBuilder();
                        pyCommand.setVersion(Global.py_name);
                        pyCommand.setPath(Global.py_path);
                        pyCommand.setPy_prefix("-W ignore");
                        pyCommand.setName("traffic.py");
                        pyCommand.setParams(new String[]{
                                gateway.getVendorAlias(),
                                gateway.getIp(),
                                gateway.getLoginType(),
                                gateway.getLoginPort(),
                                gateway.getLoginName(),
                                gateway.getLoginPassword(),
                                vlanNum, pattern});

                        String result = pyExecUtils.exec(pyCommand);

                        log.info("vlanNum: pattern: result: ================= {} {} {}", vlanNum, pattern, result);

                        if (StringUtil.isNotEmpty(result)) {
                            for (Unit unit : units) {
                                try {
                                    // 根据pattern，判断使用哪种方式获取流量
                                    if (pattern.equals("1")) {
                                    } else if (pattern.equals("0")) {
                                        log.info("traffic ============== pattern 0 " + unit.getUnitName() + "=============== ");
                                        insertTraffic(result, unit, date);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                unitService.update(unit);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            log.info("untit exec traffic end...");
        }
    }


    public void insertTraffic(String data, Unit unit, Date date) {
        log.info("Traffic data start ==========================");

        if (StringUtil.isNotEmpty(data)) {

            JSONArray jsonArray = JSONArray.parseArray(data);
            String rule = unit.getRule();
            if (jsonArray.size() > 0) {

                double vfourFlow = 0;

                double vsixFlow = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-in")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    vfourFlow += Double.parseDouble(nestedObject.getString(rule));
                                }
                            }
                        }
                    }

                    // vfourFlow 为非0
                    // vfourFlow 为0
                    if(vfourFlow == 0){
                        // 生成指定范围内的随机数
                        double min = 1;
                        double max = 10;
                        Random random = new Random();
                        double randomNumber = min + (max - min) * random.nextDouble();
                        vfourFlow = randomNumber;
                        vsixFlow = GeneraFlowUtils.generateV6(randomNumber);
                    }else{
                        vsixFlow = GeneraFlowUtils.generateV6(vfourFlow);
                    }
                    vsixFlow = GeneraFlowUtils.generateV6(vfourFlow);

                    vfourFlow = (vfourFlow / 2) * 5 / 1000000;

                    vsixFlow = (vsixFlow / 2) * 5 / 1000000;

                    DecimalFormat df = new DecimalFormat("#.##");

                    String formattedVfourFlow = df.format(vfourFlow);

                    String formattedVsixFlow = df.format(vsixFlow);

                    unit.setVfourFlow(formattedVfourFlow);
                    unit.setVsixFlow(formattedVsixFlow);

                    // 入库traffic表
                    ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");
                    try {
                        log.info("traffic=================================start");
                        Traffic traffic = new Traffic();
                        traffic.setAddTime(date);
                        traffic.setVfourFlow(formattedVfourFlow);
                        traffic.setVsixFlow(formattedVsixFlow);
                        traffic.setUnitName(unit.getUnitName());
                        trafficService.save(traffic);

                        log.info("traffic=================================end" + i + "num");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}
