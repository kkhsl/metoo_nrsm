package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TrafficFactoryImpl2 implements Gather {

    @Override
    public void executeMethod() {
        log.info("Traffic start=========");
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

                        PyCommandBuilder3 pyCommand = new PyCommandBuilder3();
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

                        // 脚本结果
                        String result = pyExecUtils.exec(pyCommand);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("Traffic end =========");
        }
    }

}
