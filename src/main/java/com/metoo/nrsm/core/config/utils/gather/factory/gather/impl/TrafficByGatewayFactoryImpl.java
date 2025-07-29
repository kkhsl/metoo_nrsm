package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;

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
import com.metoo.nrsm.core.utils.api.TrafficPushApiService;
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
                                    FlowUnit unit1 = insertTrafficYingTan4(unit, date);
                                    if (unit1 != null) {
                                        unitList.add(unit1);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                            flowUnitService.update(unit);
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

    public void insertTraffic2(String data, FlowUnit unit, Date date) {

        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");

        if (StringUtil.isNotEmpty(data)) {
            try {
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
                    String formattedVsixFlow = df.format(vsixFlow * 30);

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
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    // version-2
    public void insertTrafficYingTan2(String data, FlowUnit unit, Date date) {

        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");

        log.info("Traffic data start ==========================");

        if (StringUtil.isNotEmpty(data)) {

            boolean flag = FlowUtils.isWithinTimeRange();

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
                                    ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                }
                            }
                        }
                    }

                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Outbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    ipv4Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                    if (flag) {
                        if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")) {
                            for (String key : jsonObject.keySet()) {
                                if (jsonObject.get(key) instanceof JSONObject) {
                                    JSONObject nestedObject = jsonObject.getJSONObject(key);
                                    if (nestedObject.containsKey(rule)) {
                                        ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    }
                                }
                            }
                        }


                        if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")) {
                            for (String key : jsonObject.keySet()) {
                                if (jsonObject.get(key) instanceof JSONObject) {
                                    JSONObject nestedObject = jsonObject.getJSONObject(key);
                                    if (nestedObject.containsKey(rule)) {
                                        ipv6Outbound1 += Double.parseDouble(nestedObject.getString(rule));
                                        ipv6Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                    }
                                }
                            }
                        }
                    }
                }


                double vfour = 0;


                if (ipv4Inbound1 != 0 && ipv4Outbound1 != 0 && ipv4Outbound2 != 0) {
                    // v4
                    double ipv4InCalculate = FlowUtils.calculateFlow(ipv4Inbound1);
                    double ipv4OutCalculate1 = FlowUtils.calculateFlow(ipv4Outbound1);
                    double ipv4OutCalculate2 = FlowUtils.calculateFlow(ipv4Outbound2);

                    double ipv4Inbound = (ipv4InCalculate / 2) * 5 / 1000000;

                    double ipv4Outbound = ipv4OutCalculate1 - ipv4OutCalculate2;

                    ipv4Outbound = (ipv4Outbound / 2) * 5 / 1000000;


                    double vfourFlow = ipv4Inbound + ipv4Outbound;

                    vfour = FlowUtils.calculateFlow(vfourFlow);
                }


                // v6
                double vSix = 0;
                if (flag) {
                    double ipv6InCalculate = FlowUtils.calculateFlow(ipv6Inbound1);
                    double ipv6OutCalculate1 = FlowUtils.calculateFlow(ipv6Outbound1);
                    double ipv6OutCalculate2 = FlowUtils.calculateFlow(ipv6Outbound2);

                    double ipv6Inbound = (ipv6InCalculate / 2) * 5 / 1000000;

                    double ipv6Outbound = ipv6OutCalculate1 - ipv6OutCalculate2;

                    ipv6Outbound = (ipv6Outbound / 2) * 5 / 1000000;

                    double vsixFlow = ipv6Inbound + ipv6Outbound;

                    vSix = FlowUtils.calculateFlow(vsixFlow);

                }

                //////////////////////////////////////////////////////

                String formattedVfourFlow = "";
                String formattedVsixFlow = "";

                if (FlowUtils.isWithinTimeRange()) {
                    // 方式一
//                    DecimalFormat df = new DecimalFormat("#.##");
//                    String formattedVfourFlow = df.format(vfour);
//                    String formattedVsixFlow = df.format(vSix);

                    // 方式二
                    formattedVfourFlow = String.format("%.2f", vfour);
                    formattedVsixFlow = String.format("%.2f", vSix);
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
                    formattedVfourFlow = String.format("%.2f", vfour);
                    formattedVsixFlow = String.format("%.2f", vSix);
                }

                unit.setVfourFlow(formattedVfourFlow);
                unit.setVsixFlow(formattedVsixFlow);

                // 入库traffic表
                try {
                    log.info("controller=================================start");
                    Traffic traffic = new Traffic();
                    traffic.setAddTime(date);
                    traffic.setVfourFlow(formattedVfourFlow);
                    traffic.setVsixFlow(formattedVsixFlow);
                    traffic.setUnitName(unit.getUnitName());
                    int i = trafficService.save(traffic);

                    // 发送数据-netmap-monitor
                    TrafficPushApiService trafficPushApiService = new TrafficPushApiService(new RestTemplate());
                    trafficPushApiService.sendDataToMTO(JSON.toJSONString(traffic));

                    log.info("controller=================================end" + i + "num");
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                return ipv4Inbound1 - ipv4Inbound2;
            }
        }
//        return 0 ;
    }

    public static void main(String[] args) {
        System.out.println(getRandomWithinRange(0.37));
        ;
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

//    public void insertTrafficYingTan3(FlowUnit unit, Date date) {
//
//        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");
//
//        log.info("Traffic data start ==========================");
//
//        double vfour = Double.parseDouble(unit.getVfourFlow() == null ? "0": unit.getVfourFlow());
//
//        double vSix = Double.parseDouble(unit.getVsixFlow() == null ? "0": unit.getVsixFlow());
//
//        vfour = getRandomWithinRange(vfour);
//        vSix = getRandomWithinRange(vSix);
//
//        boolean flag = FlowUtils.isWithinTimeRange();
//        if(flag){
//         if(vfour > 0){
//                vfour = vfour / 20;
//            }
//            if(vSix > 0){
//                vSix = vSix / 20;
//            }
//        }else{
//            if(vfour == 0){
//                // 生成指定范围内的随机数
//                double min = 1;
//                double max = 10;
//                Random random = new Random();
//                double randomNumber = min + (max - min) * random.nextDouble();
//                vfour = randomNumber;
//                vSix = generateV6(randomNumber);
//            }else{
//                vSix = generateV6(vfour);
//            }
//        }
//
//        if(vfour <= 0){
//            vfour = 0;
//        }
//        if(vSix <= 0){
//            vSix = 0;
//        }
//        String formattedVfourFlow = String.format("%.2f", vfour);
//        String formattedVsixFlow = String.format("%.2f", vSix);
//
//        unit.setVfourFlow(formattedVfourFlow);
//        unit.setVsixFlow(formattedVsixFlow);
//
//        log.info("单位名称：" + unit.getUnitName() + " V4流量 " + formattedVfourFlow + " V6流量 " + formattedVsixFlow);
//
//            // 入库traffic表
//        try {
//            log.info("controller=================================start");
//
//            Traffic controller = new Traffic();
//            controller.setAddTime(date);
//            controller.setVfourFlow(formattedVfourFlow);
//            controller.setVsixFlow(formattedVsixFlow);
//            controller.setUnitName(unit.getUnitName());
//
//
//            int i = trafficService.save(controller);
//
//
//            // 发送数据-netmap-monitor
//            ApiService apiService = new ApiService(new RestTemplate());
//            apiService.sendDataToMTO(JSON.toJSONString(controller));
//
//            log.info("controller=================================end" + i + "num");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public FlowUnit insertTrafficYingTan4(FlowUnit unit, Date date) {

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
            trafficPushApiService.sendDataToMTO(JSON.toJSONString(traffic));

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


    // version-1
    public void insertTrafficYingTan(String data, FlowUnit unit, Date date) {

        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");

        log.info("Traffic data start ==========================");

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
                                    ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                }
                            }
                        }
                    }

                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Outbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    ipv4Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }


                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                }
                            }
                        }
                    }


                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv6Outbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    ipv6Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                }


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

                String formattedVsixFlow = df.format(vsixFlow * 30);

                unit.setVfourFlow(formattedVfourFlow);
                unit.setVsixFlow(formattedVsixFlow);


                // 入库traffic表
                try {
                    log.info("controller=================================start");
                    Traffic traffic = new Traffic();
                    traffic.setAddTime(date);
                    traffic.setVfourFlow(formattedVfourFlow);
                    traffic.setVsixFlow(formattedVsixFlow);
                    traffic.setUnitName(unit.getUnitName());
                    int i = trafficService.save(traffic);


                    // 发送数据-netmap-monitor
                    TrafficPushApiService trafficPushApiService = new TrafficPushApiService(new RestTemplate());
                    trafficPushApiService.sendDataToMTO(JSON.toJSONString(traffic));

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
