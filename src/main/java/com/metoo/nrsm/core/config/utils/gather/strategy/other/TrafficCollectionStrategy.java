package com.metoo.nrsm.core.config.utils.gather.strategy.other;

import com.alibaba.fastjson.JSON;
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
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.Traffic;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 14:49
 * <p>
 * ipv4 port 采集
 */
@Slf4j
@Component
public class TrafficCollectionStrategy implements DataCollectionStrategy {

    private final IFlowUnitService flowUnitService;
    private final IGatewayService gatewayService;
    private final ITrafficService trafficService;
    private final PyExecUtils pyExecUtils;

    @Autowired
    public TrafficCollectionStrategy(IFlowUnitService flowUnitService, IGatewayService gatewayService, ITrafficService trafficService,
                                     PyExecUtils pyExecUtils) {
        this.flowUnitService = flowUnitService;
        this.gatewayService = gatewayService;
        this.trafficService = trafficService;
        this.pyExecUtils = pyExecUtils;
    }

    @Override
    public void collectData(Context context) {
        try {
            FlowUnit unit = (FlowUnit) context.getEntity();
            Date date = context.getAddTime();
            if (unit != null) {
//                unit.setHidden(false);
                if(StringUtil.isNotEmpty(String.valueOf(unit.getGatewayId()))){
                    Gateway gateway = gatewayService.selectObjById(unit.getGatewayId());
                    if(gateway != null){
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
                                unit.getVlanNum(), unit.getPattern()});
                        String result = this.pyExecUtils.exec(pyCommand);
                        if (StringUtil.isNotEmpty(result)) {
                            try {
                                // 根据pattern，判断使用哪种方式获取流量
                                if(unit.getPattern().equals("1")){
//                                    unit.setHidden(true);
                                    this.insertTraffic2(result, unit, date);

                                }else if(unit.getPattern().equals("0")){
                                    log.info("traffic ============== pattern 0 =============== ");
//                                    unit.setHidden(true);
//                                    this.insertTraffic(result, unit, date);

                                    log.info("traffic - data " + result);

                                    this.insertTrafficYingTan(result, unit, date);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                flowUnitService.update(unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    [{"Type": "sum-ipv4-out", "1/7": {"1": "0", "2": "0", "3": "0", "4": "0", "5": "0", "6": "0"}, "1/8": {"1": "1640", "2": "0", "3": "16", "4": "0", "5": "37720", "6": "0"}, "2/7": {"1": "0", "2": "0", "3": "0", "4": "0", "5": "0", "6": "0"}, "2/8": {"1": "4040", "2": "0", "3": "8", "4": "0", "5": "1864", "6": "0"}}, {"Type": "sum-ipv6-out", "1/7": {"1": "0", "2": "0", "3": "0", "4": "0", "5": "0", "6": "0"}, "1/8": {"1": "0", "2": "0", "3": "0", "4": "0", "5": "0", "6": "0"}, "2/7": {"1": "0", "2": "0", "3": "0", "4": "0", "5": "0", "6": "0"}, "2/8": {"1": "0", "2": "0", "3": "0", "4": "0", "5": "32", "6": "0"}}, {"Type": "sum-ipv4-in", "1/7": {"1": "0", "3": "0", "5": "0"}, "1/8": {"1": "61040", "3": "8", "5": "60112"}, "2/7": {"1": "0", "3": "0", "5": "0"}, "2/8": {"1": "272", "3": "0", "5": "124560"}}, {"Type": "sum-ipv6-in", "1/7": {"1": "0", "3": "0", "5": "0"}, "1/8": {"1": "0", "3": "0", "5": "24"}, "2/7": {"1": "0", "3": "0", "5": "0"}, "2/8": {"1": "0", "3": "0", "5": "0"}}]



    public void insertTrafficYingTan(String outData, String inData, FlowUnit unit, Date date){

        double vfourFlow = vfourFlow(outData, inData, unit, date);
        double vsixFlow = vsixFlow(outData, inData, unit, date);

        DecimalFormat df = new DecimalFormat("#.##");

        String formattedVfourFlow = df.format(vfourFlow);

        String formattedVsixFlow = df.format(vsixFlow);

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





    public double vfourFlow(String outData, String inData, FlowUnit unit, Date date){
        double vfourFlowOut = vfourFlowOut(outData, unit, date);

        return 0;
    }





    public double vsixFlow(String data, String inData, FlowUnit unit, Date date){
        double vsixFlowOut = vsixFlowOut(data, unit, date);

        return 0;
    }


    // 共享
    public double vfourFlowOut(String outData, FlowUnit unit, Date date){
        double ipv4Outbound = 0;
        if(StringUtil.isNotEmpty(outData)) {
            JSONArray jsonArray = JSONArray.parseArray(outData);
            String rule = unit.getRule();
            if (jsonArray.size() > 0) {

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")) {
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Outbound += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }
                }
            }
        }
        return ipv4Outbound;
    }


    public double vsixFlowOut(String outData, FlowUnit unit, Date date){
        double ipv6Outbound = 0;
        if(StringUtil.isNotEmpty(outData)){
            JSONArray jsonArray = JSONArray.parseArray(outData);
            String rule = unit.getRule();
            if(jsonArray.size() > 0){

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")){
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv6Outbound += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                }
            }
        }
        return ipv6Outbound;
    }

    @Test
    public void test(){
        String data = "[{\"Type\": \"sum-ipv4-out\", \"1/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\"}, \"1/8\": {\"1\": \"1640\", \"2\": \"0\", \"3\": \"16\", \"4\": \"0\", \"5\": \"37720\", \"6\": \"0\"}, \"2/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\"}, \"2/8\": {\"1\": \"4040\", \"2\": \"0\", \"3\": \"8\", \"4\": \"0\", \"5\": \"1864\", \"6\": \"0\"}}, {\"Type\": \"sum-ipv6-out\", \"1/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\"}, \"1/8\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\"}, \"2/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\"}, \"2/8\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"32\", \"6\": \"0\"}}, {\"Type\": \"sum-ipv4-in\", \"1/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\"}, \"1/8\": {\"1\": \"61040\", \"3\": \"8\", \"5\": \"60112\"}, \"2/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\"}, \"2/8\": {\"1\": \"272\", \"3\": \"0\", \"5\": \"124560\"}}, {\"Type\": \"sum-ipv6-in\", \"1/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\"}, \"1/8\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"24\"}, \"2/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\"}, \"2/8\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\"}}]";
        insertTrafficYingTan(data, new FlowUnit(), new Date());
    }


    public void insertTrafficYingTan(String data, FlowUnit unit, Date date){

        log.info("traffic - data - start ==========================");
        if(StringUtil.isNotEmpty(data)){

            JSONArray jsonArray = JSONArray.parseArray(data);
            String rule = unit.getRule();
            if(jsonArray.size() > 0){

                double ipv4Inbound1 = 0;

                double ipv4Outbound1 = 0;
                double ipv4Outbound2 = 0;

                double ipv6Inbound1 = 0;

                double ipv6Outbound1 = 0;
                double ipv6Outbound2 = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-in")){
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                }
                            }
                        }
                    }

                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")){
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


                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")){
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                }
                            }
                        }
                    }



                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")){
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
                System.out.println("ipv4Inbound1: " + ipv4Inbound1);

                double ipv4Inbound = (ipv4Inbound1/2) * 5 / 1000000;


                double ipv4Outbound = ipv4Outbound1 - ipv4Outbound2;
                ipv4Outbound = (ipv4Outbound/2) * 5 / 1000000;

                double vfourFlow = 0.01;

                if((ipv4Outbound  < 0.01 && ipv4Outbound > 0) && (ipv4Inbound  < 0.01 && ipv4Inbound > 0)){
                    vfourFlow = 0.01;
                }else{
                    if (ipv4Outbound  < 0.01 && ipv4Outbound > 0) {
                        ipv4Outbound = 0.01;
                    }
                    if (ipv4Inbound  < 0.01 && ipv4Inbound > 0) {
                        ipv4Inbound = 0.01;
                    }
                    vfourFlow = ipv4Inbound + ipv4Outbound;
                }


                double ipv6Inbound = (ipv6Inbound1/2) * 5 / 1000000;

                double ipv6Outbound = ipv6Outbound1 - ipv6Outbound2;

                ipv6Outbound =  (ipv6Outbound/2) * 5 / 1000000;

                double vsixFlow = 0.01;

                if((ipv6Outbound  < 0.01 && ipv6Outbound > 0) && (ipv6Inbound  < 0.01 && ipv6Inbound > 0)){
                    vsixFlow = 0.01;
                }else{
                    if (ipv6Outbound  < 0.01 && ipv6Outbound > 0) {
                        ipv6Outbound = 0.01;
                    }
                    if (ipv6Inbound  < 0.01 && ipv6Inbound > 0) {
                        ipv6Inbound = 0.01;
                    }
                    vsixFlow = ipv6Inbound + ipv6Outbound;
                }

                DecimalFormat df = new DecimalFormat("#.##");

                String formattedVfourFlow = df.format(vfourFlow);

                String formattedVsixFlow = df.format(vsixFlow * 10);

                unit.setVfourFlow(formattedVfourFlow);
                unit.setVsixFlow(formattedVsixFlow);

                log.info("traffic - data - start - vfourFlow ==========================" + vfourFlow);

                log.info("traffic - data - start - vsixFlow ==========================" + vsixFlow);

                log.info("traffic - data - start - ipv6Inbound ==========================" + ipv6Inbound);

                log.info("traffic - data - start - ipv6Outbound ==========================" + ipv6Outbound);

                log.info("traffic - data - end - formattedVfourFlow  ==========================" + formattedVfourFlow);

                log.info("traffic - data - end - formattedVsixFlow  ==========================" + formattedVsixFlow);

                // 入库traffic表
                try {
                    log.info("traffic=================================start");
                    Traffic traffic = new Traffic();
                    traffic.setAddTime(date);
                    traffic.setVfourFlow(formattedVfourFlow);
                    traffic.setVsixFlow(formattedVsixFlow);
                    traffic.setUnitName(unit.getUnitName());
                    int i = trafficService.save(traffic);


                    // 发送数据-netmap-monitor
                    ApiService apiService = new ApiService(new RestTemplate());
                    apiService.sendDataToMTO(JSON.toJSONString(traffic));

                    log.info("traffic=================================end" + i + "num");
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                return ipv4Inbound1 - ipv4Inbound2;
            }
        }
//        return 0 ;
    }


    // 共享
    public void insertTraffic(String data, FlowUnit unit, Date date){
        if(StringUtil.isNotEmpty(data)){
            JSONArray jsonArray = JSONArray.parseArray(data);
            String rule = unit.getRule();
            if(jsonArray.size() > 0){

                double ipv4Inbound1 = 0;
                double ipv4Inbound2 = 0;

                double ipv4Outbound1 = 0;
                double ipv4Outbound2 = 0;

                double ipv6Inbound1 = 0;
                double ipv6Inbound2 = 0;

                double ipv6Outbound1 = 0;
                double ipv6Outbound2 = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-in")){
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    ipv4Inbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }

                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")){
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


                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")){
                        for (String key : jsonObject.keySet()) {
                            if (jsonObject.get(key) instanceof JSONObject) {
                                JSONObject nestedObject = jsonObject.getJSONObject(key);
                                if (nestedObject.containsKey(rule)) {
                                    ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
                                    ipv6Inbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
                                }
                            }
                        }
                    }



                    if(jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")){
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
                System.out.println("ipv4Inbound1: " + ipv4Inbound1);
                System.out.println("ipv4Inbound2: " + ipv4Inbound2);

                ipv4Inbound1 = (ipv4Inbound1/2) * 5 / 1000000;
                ipv4Inbound2 = (ipv4Inbound2/2) * 5 / 1000000;


                double a = ipv4Inbound1 - ipv4Inbound2;
                double b = ipv4Outbound1 - ipv4Outbound2;

                double vfourFlow = a + b;

                ipv6Inbound1 = (ipv6Inbound1/2) * 5 / 1000000;
                ipv6Inbound2 = (ipv6Inbound2/2) * 5 / 1000000;


                double c = ipv6Inbound1 - ipv6Inbound2;
                double d = ipv6Outbound1 - ipv6Outbound2;



                double vsixFlow = c + d;

                DecimalFormat df = new DecimalFormat("#.##");

                String formattedVfourFlow = df.format(vfourFlow);

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

//                return ipv4Inbound1 - ipv4Inbound2;
            }
        }
//        return 0 ;
    }

    public void insertTraffic2(String data, FlowUnit unit, Date date){
        if(StringUtil.isNotEmpty(data)){
            JSONArray jsonArray = JSONArray.parseArray(data);
            if(jsonArray.size() > 0){

                double ipv4Inbound = 0;

                double ipv4Outbound = 0;

                double ipv6Inbound = 0;

                double ipv6Outbound = 0;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.get("Protocol") != null && jsonObject.get("Protocol").equals("Ipv4")){
                        if (jsonObject.containsKey("Input")) {
                            ipv4Inbound += Double.parseDouble(jsonObject.getString("Input"));
                        }
                        if (jsonObject.containsKey("Output")) {
                            ipv4Outbound += Double.parseDouble(jsonObject.getString("Output"));
                        }
                    }

                    if(jsonObject.get("Protocol") != null && jsonObject.get("Protocol").equals("Ipv6")){
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
}
