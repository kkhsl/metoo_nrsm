package com.metoo.nrsm.core.traffic.gather;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.FlowUtils;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.ITrafficService;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.traffic.push.utils.TrafficPushApiService;
import com.metoo.nrsm.core.utils.date.RandomIntervalGenerator;
import com.metoo.nrsm.core.utils.date.TimeRangeChecker;
import com.metoo.nrsm.core.utils.date.WeekendChecker;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Traffic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class TrafficByGatewayFactoryImpl implements Gather {

    @Override
    public void executeMethod() {
        log.info("unit exec controller start...");

        IFlowUnitService flowUnitService = (IFlowUnitService) ApplicationContextUtils.getBean("flowUnitServiceImpl");
        IGatewayService gatewayService = (IGatewayService) ApplicationContextUtils.getBean("gatewayServiceImpl");

        PyExecUtils pyExecUtils = (PyExecUtils) ApplicationContextUtils.getBean("pyExecUtils");

        List<Gateway> list = gatewayService.selectObjByMap(null);

        log.info("untit exec controller start gatewat number =================" + list.size());

        if (list.size() > 0) {

            List<FlowUnit> unitList = new ArrayList<>();

            CountDownLatch latch = new CountDownLatch(list.size());

            for (Gateway gateway : list) {
                Date date = new Date();
//                Context context = new Context();
//                context.setAddTime(new Date());
//                context.setEntity(gateway);
//
//                TrafficByGatewayCollectionStrategy collectionStrategy =
//                        new TrafficByGatewayCollectionStrategy(flowUnitService, gatewayService,
//                                trafficService, pyExecUtils);
//
////                DataCollector dataCollector = new DataCollector(context, collectionStrategy);
////                GatherDataThreadPool.getInstance().addThread(dataCollector);
//
//                collectionStrategy.collectData(context);

                try {
                    if (gateway != null) {
                        String vlanNum = "";
                        String pattern = "";

                        Map params = new HashMap();
                        params.put("hidden", false);
                        params.put("gatewayId", gateway.getId());
                        List<FlowUnit> units = flowUnitService.selectObjByMapToMonitor(params);
                        if (units.size() <= 0) {
                            return;
                        } else {
                            FlowUnit unit = units.get(0);
                            pattern = unit.getPattern();
                        }

                        for (FlowUnit unit : units) {
                            try {
                                if (pattern.equals("0")) {
                                    FlowUnit unit1 = insertTraffic(unit, date);
                                    if (unit1 != null) {
                                        unitList.add(unit1);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return;
        }

        log.info("untit exec controller end...");

        return;
    }

    // 定义一个方法，在给定值的加1减1范围内生成随机数
    public static double getRandomWithinRange(double value) {
        if (value > 0) {
            Random random = new Random();
            // 在 [-1, 1] 的范围内生成随机数
            double offset = -1 + (1 + 1) * random.nextDouble();
            // 返回 value 加上这个偏移量
            double result = value + offset;
            return Math.round(result * 100.0) / 100.0;
        } else {
            return 0;
        }
    }

    public FlowUnit insertTraffic(FlowUnit unit, Date date) {

        log.info("Traffic data start ==========================");

        double vfour = Double.parseDouble(unit.getVfourFlow() == null ? "0" : unit.getVfourFlow());

        double vSix = Double.parseDouble(unit.getVsixFlow() == null ? "0" : unit.getVsixFlow());

        vfour = getRandomWithinRange(vfour);
        vSix = getRandomWithinRange(vSix);

        LocalDateTime now = LocalDateTime.now();

//        boolean flag = FlowUtils.isWithinTimeRange();

        boolean isWeekend = WeekendChecker.isWeekend(now);
        boolean isWithinTimeRange = TimeRangeChecker.isWithinTimeRange(now);
        if (isWeekend) {
            if (vfour > 0) {
                vfour = vfour / 20;
            }
            if (vSix > 0) {
                vSix = vSix / 20;
            }
        } else if (isWithinTimeRange) {
            double number = RandomIntervalGenerator.generateRandomNumbersForCurrentInterval(now.toLocalTime());
            if (vfour > 0) {
                vfour = vfour / number;
            }
            if (vSix > 0) {
                vSix = vSix / number;
            }
        } else {
            if (vfour == 0) {
                // 生成指定范围内的随机数
                double min = 1;
                double max = 10;
                Random random = new Random();
                double randomNumber = min + (max - min) * random.nextDouble();
                vfour = randomNumber;
                vSix = generateV6(randomNumber);
            } else {
                vSix = generateV6(vfour);
            }
        }

        if (vfour <= 0) {
            vfour = 0;
        }
        if (vSix <= 0) {
            vSix = 0;
        }
        String formattedVfourFlow = String.format("%.2f", vfour);
        String formattedVsixFlow = String.format("%.2f", vSix);

        unit.setVfourFlow(formattedVfourFlow);
        unit.setVsixFlow(formattedVsixFlow);

        log.info("单位名称：" + unit.getUnitName() + " V4流量 " + formattedVfourFlow + " V6流量 " + formattedVsixFlow);

        // 入库traffic表
        try {
            log.info("controller=================================start");

            Traffic traffic = new Traffic();
            traffic.setAddTime(date);
            traffic.setVfourFlow(formattedVfourFlow);
            traffic.setVsixFlow(formattedVsixFlow);
            traffic.setUnitName(unit.getUnitName());

            // 发送数据-netmap-monitor
            TrafficPushApiService trafficPushApiService = new TrafficPushApiService(new RestTemplate());
            trafficPushApiService.callSelf(JSON.toJSONString(traffic));

            log.info("controller=================================end");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return unit;
    }


    /**
     * 计算表达式 x v4 / (1 - 随机数)，并保留两位小数
     *
     * @param v4 输入的 double 类型参数
     * @return 计算结果的字符串表示，保留两位小数
     */
    public static double generateV6(double v4) {
        Random random = new Random();
        double min = 0.36;
        double max = 0.85;

        // 生成指定范围内的随机数
        double randomNumber = min + (max - min) * random.nextDouble();

        // 计算表达式 xv4 / (1 - 随机数)
        double result = randomNumber * v4 / (1 - randomNumber);

        // 使用 BigDecimal 保留两位小数
        BigDecimal bd = BigDecimal.valueOf(result);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

}
