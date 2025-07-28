package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.FlowUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.utils.GeneraFlowUtils;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Gateway;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.stereotype.Component;

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
                        pyCommand.setName("main.py");
                        pyCommand.setParams(new String[]{
                                gateway.getVendorAlias(),
                                gateway.getIp(),
                                gateway.getLoginType(),
                                gateway.getLoginPort(),
                                gateway.getLoginName(),
                                gateway.getLoginPassword(),
                                "vlan_policy"});

                        String result = pyExecUtils.exec(pyCommand);

                        log.info("Traffic result: ================= {} ", result);

                        if (StringUtil.isNotEmpty(result)) {
                            for (FlowUnit unit : units) {
                                try {
                                    // 根据pattern，判断使用哪种方式获取流量
                                    if (pattern.equals("1")) {
                                    } else if (pattern.equals("0")) {
//                                        insertTraffic(result, unit);
                                        unit.setAddTime(date);
                                        flowUnitService.update(unit);
                                    }
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


}
