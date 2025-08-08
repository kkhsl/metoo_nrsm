package com.metoo.nrsm.core.traffic.gather.ganzhou;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.FlowUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.utils.GeneraFlowUtils;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.entity.FlowUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TrafficFactoryGanzhouSelfImpl implements Gather {

    public void executeMethod() {
        log.info("Traffic start=========");
        IFlowUnitService flowUnitService = (IFlowUnitService) ApplicationContextUtils.getBean("flowUnitServiceImpl");
        Map params = new HashMap();
        params.put("hidden", false);
        List<FlowUnit> units = flowUnitService.selectObjByMap(params);
        if (units.size() <= 0) {
            return;
        } else {
            for (FlowUnit unit : units) {
                this.insertTraffic(unit);
                flowUnitService.update(unit);
            }
        }
    }

    public void insertTraffic(FlowUnit unit) {
        double vfourFlowTotal = 0.0D;
        vfourFlowTotal = this.generateVfourFlowTotal(unit.getV4Traffic());
        double vfourFlow = this.generateV4(vfourFlowTotal);
        double vsixFlow = this.generateV6(vfourFlow, unit.getRandom());
        String formattedVfourFlow = "";
        String formattedVsixFlow = "";
        if (vfourFlow < 0.01D) {
            vfourFlow = 0.01D;
        }

        if (vsixFlow < 0.01D) {
            vsixFlow = 0.01D;
        }

        formattedVfourFlow = String.format("%.2f", vfourFlow);
        formattedVsixFlow = String.format("%.2f", vsixFlow);
        unit.setVfourFlow(formattedVfourFlow);
        unit.setVsixFlow(formattedVsixFlow);
        log.info("单位名称：{}  v4流量：{} v6流量：{}", new Object[]{unit.getUnitName(), formattedVfourFlow, formattedVsixFlow});
    }


    public double generateV6(double vfourFlow, String random) {
        double vsixFlow = 0.1D;
        if (StringUtil.isNotEmpty(random)) {
            vsixFlow = GeneraFlowUtils.generateV6(vfourFlow, random);
        } else {
            vsixFlow = GeneraFlowUtils.generateV6(vfourFlow);
        }

        return vsixFlow;
    }

    public double generateVfourFlowTotal(String randomRadius) {
        double vfourFlowTotal = GeneraFlowUtils.generatev4Traffic(randomRadius);
        if (FlowUtils.isWithinTimeRange()) {
            vfourFlowTotal /= 20.0D;
        }

        return vfourFlowTotal;
    }

    public double generateV4(double vfourFlow) {
        double min;
        double max;
        Random random;
        double randomNumber;
        if (FlowUtils.isWithinTimeRange()) {
            if (vfourFlow == 0.0D) {
                min = 0.1D;
                max = 1.0D;
                random = new Random();
                randomNumber = min + (max - min) * random.nextDouble();
                vfourFlow = randomNumber;
            } else {
                vfourFlow = vfourFlow / 2.0D * 5.0D / 1000000.0D;
                if (vfourFlow < 0.01D) {
                    vfourFlow = 0.01D;
                }
            }
        } else if (vfourFlow == 0.0D) {
            min = 1.0D;
            max = 10.0D;
            random = new Random();
            randomNumber = min + (max - min) * random.nextDouble();
            vfourFlow = randomNumber;
        } else {
            vfourFlow = vfourFlow / 2.0D * 5.0D / 1000000.0D;
            if (vfourFlow < 0.01D) {
                vfourFlow = 0.01D;
            }
        }

        return vfourFlow;
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

}
