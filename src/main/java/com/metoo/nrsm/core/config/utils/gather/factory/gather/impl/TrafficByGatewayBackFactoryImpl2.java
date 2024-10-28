//package com.metoo.nrsm.core.config.utils.gather.factory.gather.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.github.pagehelper.util.StringUtil;
//import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
//import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder;
//import com.metoo.nrsm.core.config.utils.gather.factory.gather.FlowUtils;
//import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
//import com.metoo.nrsm.core.config.utils.gather.strategy.Context;
//import com.metoo.nrsm.core.config.utils.gather.strategy.DataCollector;
//import com.metoo.nrsm.core.config.utils.gather.strategy.other.TrafficByGatewayCollectionStrategy;
//import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
//import com.metoo.nrsm.core.service.IGatewayService;
//import com.metoo.nrsm.core.service.ITrafficService;
//import com.metoo.nrsm.core.service.IUnitService;
//import com.metoo.nrsm.core.utils.Global;
//import com.metoo.nrsm.core.utils.api.ApiService;
//import com.metoo.nrsm.core.utils.gather.thread.GatherDataThreadPool;
//import com.metoo.nrsm.entity.Gateway;
//import com.metoo.nrsm.entity.Traffic;
//import com.metoo.nrsm.entity.Unit;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.util.*;
//import java.util.concurrent.CountDownLatch;
//
//@Slf4j
//@Component
//public class TrafficByGatewayBackFactoryImpl2 implements Gather {
//
//    @Override
//    public List<Unit> executeMethod() {
//        this.executeMethodw();
//        return null;}
//
//    public void executeMethodw() {
//        log.info("unit exec traffic start...");
//
//        IUnitService unitService = (IUnitService) ApplicationContextUtils.getBean("unitServiceImpl");
//        IGatewayService gatewayService = (IGatewayService) ApplicationContextUtils.getBean("gatewayServiceImpl");
//        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");
//        PyExecUtils pyExecUtils = (PyExecUtils) ApplicationContextUtils.getBean("pyExecUtils");
//
//        List<Gateway> list = gatewayService.selectObjByMap(null);
//
//        if (list.size() > 0){
//
////            CountDownLatch latch = new CountDownLatch(list.size());
//
//            for (Gateway gateway : list) {
//                Date date = new Date();
////                Context context = new Context();
////                context.setAddTime(new Date());
////                context.setEntity(gateway);
//
////                TrafficByGatewayCollectionStrategy collectionStrategy =
////                        new TrafficByGatewayCollectionStrategy(unitService, gatewayService,
////                                trafficService, pyExecUtils);
////
////                DataCollector dataCollector = new DataCollector(context, collectionStrategy);
////                GatherDataThreadPool.getInstance().addThread(dataCollector);
//
//                try {
//                    if (gateway != null) {
//                        String vlanNum = "";
//                        String pattern = "";
//
//                        Map params = new HashMap();
//                        params.put("hidden", false);
//                        params.put("gatewayId", gateway.getId());
//                        List<Unit> units = unitService.selectObjByMap(params);
//                        if (units.size() <= 0) {
//                            return;
//                        } else {
//                            Unit unit = units.get(0);
//                            vlanNum = unit.getVlanNum();
//                            pattern = unit.getPattern();
//                        }
//
//                        PyCommandBuilder pyCommand = new PyCommandBuilder();
//                        pyCommand.setVersion(Global.py_name);
//                        pyCommand.setPath(Global.py_path);
//                        pyCommand.setPy_prefix("-W ignore");
//                        pyCommand.setName("traffic.py");
//                        pyCommand.setParams(new String[]{
//                                gateway.getVendorAlias(),
//                                gateway.getIp(),
//                                gateway.getLoginType(),
//                                gateway.getLoginPort(),
//                                gateway.getLoginName(),
//                                gateway.getLoginPassword(),
//                                vlanNum, pattern});
//
//                        String result = pyExecUtils.exec(pyCommand);
//
////                        result = "[{\"Type\": \"sum-ipv4-out\", \"1/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\", \"7\": \"0\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"0\", \"12\": \"0\", \"13\": \"0\", \"14\": \"0\", \"15\": \"0\", \"16\": \"0\", \"17\": \"0\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"0\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"0\", \"28\": \"0\", \"29\": \"0\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"0\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"0\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"0\", \"46\": \"0\", \"47\": \"0\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"0\", \"52\": \"0\", \"53\": \"0\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"0\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"0\", \"64\": \"0\", \"65\": \"0\", \"66\": \"0\", \"67\": \"0\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"0\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"75\": \"0\", \"76\": \"0\", \"77\": \"0\", \"81\": \"0\", \"83\": \"0\", \"85\": \"0\", \"87\": \"0\", \"89\": \"0\", \"91\": \"0\", \"93\": \"0\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\"}, \"1/8\": {\"1\": \"1008\", \"2\": \"0\", \"3\": \"1192\", \"4\": \"0\", \"5\": \"624\", \"6\": \"0\", \"7\": \"61352\", \"8\": \"0\", \"9\": \"688\", \"10\": \"83592\", \"11\": \"342680\", \"12\": \"0\", \"13\": \"1522952\", \"14\": \"0\", \"15\": \"14400\", \"16\": \"0\", \"17\": \"8019328\", \"18\": \"0\", \"19\": \"4336\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"18656\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"1728\", \"28\": \"0\", \"29\": \"854544\", \"30\": \"0\", \"31\": \"32560\", \"32\": \"0\", \"33\": \"984\", \"34\": \"0\", \"35\": \"1840\", \"36\": \"0\", \"37\": \"3312\", \"38\": \"0\", \"39\": \"21488\", \"40\": \"0\", \"41\": \"2832\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"70752\", \"46\": \"0\", \"47\": \"88816\", \"48\": \"0\", \"49\": \"31696\", \"50\": \"0\", \"51\": \"2914200\", \"52\": \"0\", \"53\": \"4512\", \"54\": \"0\", \"55\": \"993464\", \"56\": \"0\", \"57\": \"55712\", \"58\": \"0\", \"59\": \"8880\", \"60\": \"0\", \"61\": \"856\", \"62\": \"0\", \"63\": \"9648\", \"64\": \"0\", \"65\": \"15896\", \"66\": \"0\", \"67\": \"696\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"152\", \"72\": \"0\", \"73\": \"7664\", \"74\": \"0\", \"75\": \"790536\", \"76\": \"0\", \"77\": \"56296\", \"81\": \"289840\", \"83\": \"57080\", \"85\": \"796024\", \"87\": \"1608\", \"89\": \"4872\", \"91\": \"5456216\", \"93\": \"36024\", \"95\": \"7920\", \"97\": \"1496\", \"99\": \"14448\"}, \"2/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\", \"7\": \"0\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"0\", \"12\": \"0\", \"13\": \"0\", \"14\": \"0\", \"15\": \"0\", \"16\": \"0\", \"17\": \"0\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"0\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"0\", \"28\": \"0\", \"29\": \"0\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"0\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"0\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"0\", \"46\": \"0\", \"47\": \"0\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"0\", \"52\": \"0\", \"53\": \"0\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"0\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"0\", \"64\": \"0\", \"65\": \"0\", \"66\": \"0\", \"67\": \"0\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"0\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"75\": \"0\", \"76\": \"0\", \"77\": \"0\", \"81\": \"0\", \"83\": \"0\", \"85\": \"0\", \"87\": \"0\", \"89\": \"0\", \"91\": \"0\", \"93\": \"0\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\"}, \"2/8\": {\"1\": \"1888\", \"2\": \"0\", \"3\": \"1768\", \"4\": \"0\", \"5\": \"4008\", \"6\": \"0\", \"7\": \"35760\", \"8\": \"0\", \"9\": \"3952\", \"10\": \"28584\", \"11\": \"69360\", \"12\": \"0\", \"13\": \"5087264\", \"14\": \"0\", \"15\": \"19400\", \"16\": \"0\", \"17\": \"684960\", \"18\": \"0\", \"19\": \"35176\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"8584\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"6624\", \"28\": \"0\", \"29\": \"2167896\", \"30\": \"0\", \"31\": \"18176\", \"32\": \"0\", \"33\": \"1008\", \"34\": \"0\", \"35\": \"6880\", \"36\": \"0\", \"37\": \"5024\", \"38\": \"0\", \"39\": \"6392\", \"40\": \"0\", \"41\": \"9760\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"113608\", \"46\": \"0\", \"47\": \"71248\", \"48\": \"0\", \"49\": \"27352\", \"50\": \"0\", \"51\": \"90144\", \"52\": \"0\", \"53\": \"14368\", \"54\": \"0\", \"55\": \"169632\", \"56\": \"0\", \"57\": \"234256\", \"58\": \"0\", \"59\": \"9104\", \"60\": \"0\", \"61\": \"14592\", \"62\": \"0\", \"63\": \"1992\", \"64\": \"0\", \"65\": \"11600\", \"66\": \"0\", \"67\": \"6632\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"1552\", \"72\": \"0\", \"73\": \"16312\", \"74\": \"0\", \"75\": \"5536376\", \"76\": \"0\", \"77\": \"60072\", \"81\": \"81128\", \"83\": \"33264\", \"85\": \"26800\", \"87\": \"4728\", \"89\": \"4640\", \"91\": \"8350320\", \"93\": \"2158672\", \"95\": \"7168\", \"97\": \"5632\", \"99\": \"1976\"}}, {\"Type\": \"sum-ipv6-out\", \"1/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\", \"7\": \"0\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"0\", \"12\": \"0\", \"13\": \"0\", \"14\": \"0\", \"15\": \"0\", \"16\": \"0\", \"17\": \"0\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"0\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"0\", \"28\": \"0\", \"29\": \"0\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"0\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"0\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"0\", \"46\": \"0\", \"47\": \"0\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"0\", \"52\": \"0\", \"53\": \"0\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"0\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"0\", \"64\": \"0\", \"65\": \"0\", \"66\": \"0\", \"67\": \"0\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"0\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"77\": \"0\", \"78\": \"0\", \"81\": \"0\", \"82\": \"0\", \"85\": \"0\", \"86\": \"0\", \"87\": \"0\", \"88\": \"0\", \"89\": \"0\", \"90\": \"0\", \"93\": \"0\", \"94\": \"0\", \"95\": \"0\", \"96\": \"0\", \"97\": \"0\", \"98\": \"0\", \"99\": \"0\"}, \"1/8\": {\"1\": \"1592\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"288\", \"6\": \"0\", \"7\": \"2232\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"870544\", \"12\": \"0\", \"13\": \"54712\", \"14\": \"0\", \"15\": \"528\", \"16\": \"0\", \"17\": \"1896\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"11000\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"48\", \"28\": \"0\", \"29\": \"4706048\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"6880\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"712\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"232\", \"44\": \"0\", \"45\": \"5168\", \"46\": \"0\", \"47\": \"1744\", \"48\": \"0\", \"49\": \"1712\", \"50\": \"0\", \"51\": \"2792\", \"52\": \"0\", \"53\": \"12392\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"9368\", \"58\": \"0\", \"59\": \"232\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"232\", \"64\": \"0\", \"65\": \"504\", \"66\": \"0\", \"67\": \"504\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"712\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"77\": \"0\", \"78\": \"0\", \"81\": \"120\", \"82\": \"0\", \"85\": \"416\", \"86\": \"0\", \"87\": \"0\", \"88\": \"0\", \"89\": \"0\", \"90\": \"0\", \"93\": \"7648\", \"94\": \"0\", \"95\": \"0\", \"96\": \"0\", \"97\": \"0\", \"98\": \"0\", \"99\": \"0\"}, \"2/7\": {\"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\", \"7\": \"0\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"0\", \"12\": \"0\", \"13\": \"0\", \"14\": \"0\", \"15\": \"0\", \"16\": \"0\", \"17\": \"0\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"0\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"0\", \"28\": \"0\", \"29\": \"0\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"0\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"0\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"0\", \"46\": \"0\", \"47\": \"0\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"0\", \"52\": \"0\", \"53\": \"0\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"0\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"0\", \"64\": \"0\", \"65\": \"0\", \"66\": \"0\", \"67\": \"0\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"0\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"77\": \"0\", \"78\": \"0\", \"81\": \"0\", \"82\": \"0\", \"85\": \"0\", \"86\": \"0\", \"87\": \"0\", \"88\": \"0\", \"89\": \"0\", \"90\": \"0\", \"93\": \"0\", \"94\": \"0\", \"95\": \"0\", \"96\": \"0\", \"97\": \"0\", \"98\": \"0\", \"99\": \"0\"}, \"2/8\": {\"1\": \"432\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"4904\", \"6\": \"0\", \"7\": \"200\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"6024\", \"12\": \"0\", \"13\": \"1385544\", \"14\": \"0\", \"15\": \"920\", \"16\": \"0\", \"17\": \"1736\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"2840\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"27848\", \"28\": \"0\", \"29\": \"8999760\", \"30\": \"0\", \"31\": \"48\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"17752\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"16808\", \"40\": \"0\", \"41\": \"544\", \"42\": \"0\", \"43\": \"216\", \"44\": \"0\", \"45\": \"6176\", \"46\": \"0\", \"47\": \"56\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"1936\", \"52\": \"0\", \"53\": \"408\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"361816\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"2832\", \"64\": \"0\", \"65\": \"424\", \"66\": \"0\", \"67\": \"1832\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"2552\", \"72\": \"0\", \"73\": \"224\", \"74\": \"0\", \"77\": \"0\", \"78\": \"0\", \"81\": \"7432\", \"82\": \"0\", \"85\": \"120\", \"86\": \"0\", \"87\": \"0\", \"88\": \"0\", \"89\": \"0\", \"90\": \"0\", \"93\": \"9776\", \"94\": \"0\", \"95\": \"0\", \"96\": \"0\", \"97\": \"48\", \"98\": \"0\", \"99\": \"0\"}}, {\"Type\": \"sum-ipv4-in\", \"1/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\", \"7\": \"0\", \"9\": \"0\", \"11\": \"0\", \"13\": \"0\", \"15\": \"0\", \"17\": \"0\", \"19\": \"0\", \"21\": \"0\", \"23\": \"0\", \"25\": \"0\", \"27\": \"0\", \"29\": \"0\", \"31\": \"0\", \"33\": \"0\", \"35\": \"0\", \"37\": \"0\", \"39\": \"0\", \"41\": \"0\", \"43\": \"0\", \"45\": \"0\", \"47\": \"0\", \"49\": \"0\", \"51\": \"0\", \"53\": \"0\", \"55\": \"0\", \"57\": \"0\", \"59\": \"0\", \"61\": \"0\", \"63\": \"0\", \"65\": \"0\", \"67\": \"0\", \"69\": \"0\", \"71\": \"0\", \"73\": \"0\", \"75\": \"0\", \"77\": \"0\", \"81\": \"0\", \"83\": \"0\", \"85\": \"0\", \"87\": \"0\", \"89\": \"0\", \"91\": \"0\", \"93\": \"0\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\", \"10\": \"0\"}, \"1/8\": {\"1\": \"2792\", \"3\": \"1712\", \"5\": \"1752\", \"7\": \"27448\", \"9\": \"3928\", \"11\": \"425848\", \"13\": \"135112\", \"15\": \"13968\", \"17\": \"23720\", \"19\": \"0\", \"21\": \"0\", \"23\": \"10800\", \"25\": \"0\", \"27\": \"1728\", \"29\": \"506920\", \"31\": \"17152\", \"33\": \"2296\", \"35\": \"8584\", \"37\": \"6392\", \"39\": \"13712\", \"41\": \"14552\", \"43\": \"0\", \"45\": \"54184\", \"47\": \"21528\", \"49\": \"17256\", \"51\": \"174952\", \"53\": \"3240\", \"55\": \"73160\", \"57\": \"179488\", \"59\": \"6344\", \"61\": \"9032\", \"63\": \"3800\", \"65\": \"12584\", \"67\": \"7800\", \"69\": \"48\", \"71\": \"152\", \"73\": \"35016\", \"75\": \"42912\", \"77\": \"130120\", \"81\": \"108376\", \"83\": \"624592\", \"85\": \"35984\", \"87\": \"4760\", \"89\": \"17808\", \"91\": \"591328\", \"93\": \"197368\", \"95\": \"6224\", \"97\": \"2584\", \"99\": \"3464\", \"10\": \"10664\"}, \"2/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\", \"7\": \"0\", \"9\": \"0\", \"11\": \"0\", \"13\": \"0\", \"15\": \"0\", \"17\": \"0\", \"19\": \"0\", \"21\": \"0\", \"23\": \"0\", \"25\": \"0\", \"27\": \"0\", \"29\": \"0\", \"31\": \"0\", \"33\": \"0\", \"35\": \"0\", \"37\": \"0\", \"39\": \"0\", \"41\": \"0\", \"43\": \"0\", \"45\": \"0\", \"47\": \"0\", \"49\": \"0\", \"51\": \"0\", \"53\": \"0\", \"55\": \"0\", \"57\": \"0\", \"59\": \"0\", \"61\": \"0\", \"63\": \"0\", \"65\": \"0\", \"67\": \"0\", \"69\": \"0\", \"71\": \"0\", \"73\": \"0\", \"75\": \"0\", \"77\": \"0\", \"81\": \"0\", \"83\": \"0\", \"85\": \"0\", \"87\": \"0\", \"89\": \"0\", \"91\": \"0\", \"93\": \"0\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\", \"10\": \"0\"}, \"2/8\": {\"1\": \"640\", \"3\": \"0\", \"5\": \"344\", \"7\": \"0\", \"9\": \"0\", \"11\": \"0\", \"13\": \"6728\", \"15\": \"0\", \"17\": \"202904\", \"19\": \"11064\", \"21\": \"0\", \"23\": \"0\", \"25\": \"408\", \"27\": \"5736\", \"29\": \"0\", \"31\": \"0\", \"33\": \"0\", \"35\": \"0\", \"37\": \"0\", \"39\": \"0\", \"41\": \"0\", \"43\": \"0\", \"45\": \"56304\", \"47\": \"109072\", \"49\": \"7776\", \"51\": \"23480\", \"53\": \"11464\", \"55\": \"0\", \"57\": \"606432\", \"59\": \"13208\", \"61\": \"0\", \"63\": \"1016\", \"65\": \"6104\", \"67\": \"0\", \"69\": \"0\", \"71\": \"0\", \"73\": \"6384\", \"75\": \"2211232\", \"77\": \"0\", \"81\": \"32312\", \"83\": \"0\", \"85\": \"7536\", \"87\": \"0\", \"89\": \"0\", \"91\": \"0\", \"93\": \"2376\", \"95\": \"0\", \"97\": \"0\", \"99\": \"7336\", \"10\": \"40288\"}}, {\"Type\": \"sum-ipv6-in\", \"1/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\", \"7\": \"0\", \"9\": \"0\", \"11\": \"0\", \"13\": \"0\", \"15\": \"0\", \"17\": \"0\", \"19\": \"0\", \"21\": \"0\", \"23\": \"0\", \"25\": \"0\", \"27\": \"0\", \"29\": \"0\", \"31\": \"0\", \"33\": \"0\", \"35\": \"0\", \"37\": \"0\", \"39\": \"0\", \"41\": \"0\", \"43\": \"0\", \"45\": \"0\", \"47\": \"0\", \"49\": \"0\", \"51\": \"0\", \"53\": \"0\", \"55\": \"0\", \"57\": \"0\", \"59\": \"0\", \"61\": \"0\", \"63\": \"0\", \"65\": \"0\", \"67\": \"0\", \"69\": \"0\", \"71\": \"0\", \"73\": \"0\", \"77\": \"0\", \"81\": \"0\", \"85\": \"0\", \"87\": \"0\", \"89\": \"0\", \"93\": \"0\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\", \"10\": \"0\"}, \"1/8\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"6160\", \"7\": \"11808\", \"9\": \"0\", \"11\": \"93736\", \"13\": \"53728\", \"15\": \"7592\", \"17\": \"1888\", \"19\": \"0\", \"21\": \"0\", \"23\": \"3984\", \"25\": \"0\", \"27\": \"0\", \"29\": \"318120\", \"31\": \"952\", \"33\": \"2712\", \"35\": \"2893456\", \"37\": \"0\", \"39\": \"6056\", \"41\": \"608\", \"43\": \"304\", \"45\": \"272\", \"47\": \"1056\", \"49\": \"7072\", \"51\": \"2192\", \"53\": \"0\", \"55\": \"0\", \"57\": \"7608\", \"59\": \"0\", \"61\": \"0\", \"63\": \"8224\", \"65\": \"808\", \"67\": \"2792\", \"69\": \"0\", \"71\": \"4416\", \"73\": \"784\", \"77\": \"0\", \"81\": \"0\", \"85\": \"2408\", \"87\": \"0\", \"89\": \"0\", \"93\": \"14128\", \"95\": \"0\", \"97\": \"784\", \"99\": \"0\", \"10\": \"280\"}, \"2/7\": {\"1\": \"0\", \"3\": \"0\", \"5\": \"0\", \"7\": \"0\", \"9\": \"0\", \"11\": \"0\", \"13\": \"0\", \"15\": \"0\", \"17\": \"0\", \"19\": \"0\", \"21\": \"0\", \"23\": \"0\", \"25\": \"0\", \"27\": \"0\", \"29\": \"0\", \"31\": \"0\", \"33\": \"0\", \"35\": \"0\", \"37\": \"0\", \"39\": \"0\", \"41\": \"0\", \"43\": \"0\", \"45\": \"0\", \"47\": \"0\", \"49\": \"0\", \"51\": \"0\", \"53\": \"0\", \"55\": \"0\", \"57\": \"0\", \"59\": \"0\", \"61\": \"0\", \"63\": \"0\", \"65\": \"0\", \"67\": \"0\", \"69\": \"0\", \"71\": \"0\", \"73\": \"0\", \"77\": \"0\", \"81\": \"0\", \"85\": \"0\", \"87\": \"0\", \"89\": \"0\", \"93\": \"0\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\", \"10\": \"0\"}, \"2/8\": {\"1\": \"2304\", \"3\": \"0\", \"5\": \"0\", \"7\": \"0\", \"9\": \"0\", \"11\": \"0\", \"13\": \"0\", \"15\": \"0\", \"17\": \"4608\", \"19\": \"0\", \"21\": \"0\", \"23\": \"0\", \"25\": \"0\", \"27\": \"3296\", \"29\": \"0\", \"31\": \"0\", \"33\": \"0\", \"35\": \"0\", \"37\": \"0\", \"39\": \"0\", \"41\": \"0\", \"43\": \"312\", \"45\": \"4976\", \"47\": \"816\", \"49\": \"0\", \"51\": \"7440\", \"53\": \"38536\", \"55\": \"0\", \"57\": \"18864\", \"59\": \"1864\", \"61\": \"0\", \"63\": \"648\", \"65\": \"149776\", \"67\": \"0\", \"69\": \"0\", \"71\": \"0\", \"73\": \"0\", \"77\": \"0\", \"81\": \"2152\", \"85\": \"976\", \"87\": \"0\", \"89\": \"0\", \"93\": \"1896\", \"95\": \"0\", \"97\": \"0\", \"99\": \"0\", \"10\": \"0\"}}]";
//
//                        log.info("vlanNum: pattern: result: ================= {} {} {}", vlanNum, pattern, result);
//
//                        if (StringUtil.isNotEmpty(result)) {
//                            for (Unit unit : units) {
//                                try {
//                                    // 根据pattern，判断使用哪种方式获取流量
//                                    if (pattern.equals("1")) {
//                                        log.info("traffic ============== pattern 1 " + unit.getUnitName() + "=============== ");
//                                        insertTraffic2(result, unit, date);
//                                    } else if (pattern.equals("0")) {
//                                        log.info("traffic ============== pattern 0 " + unit.getUnitName() + "=============== ");
//                                        insertTrafficYingTan(result, unit, date);
////                                        insertTrafficYingTan2(result, unit, date);
////                                        insertTrafficYingTan3(unit, date);
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                unitService.update(unit);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
////            try {
////
////                latch.await();// 等待结果线程池线程执行结束
////
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//        }
//
//        log.info("untit exec traffic end...");
//    }
//
//    public void insertTraffic2(String data, Unit unit, Date date) {
//
//        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");
//
//        if (StringUtil.isNotEmpty(data)) {
//            try {
//                JSONArray jsonArray = JSONArray.parseArray(data);
//                if (jsonArray.size() > 0) {
//
//                    double ipv4Inbound = 0;
//
//                    double ipv4Outbound = 0;
//
//                    double ipv6Inbound = 0;
//
//                    double ipv6Outbound = 0;
//
//                    for (int i = 0; i < jsonArray.size(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        if (jsonObject.get("Protocol") != null && jsonObject.get("Protocol").equals("Ipv4")) {
//                            if (jsonObject.containsKey("Input")) {
//                                ipv4Inbound += Double.parseDouble(jsonObject.getString("Input"));
//                            }
//                            if (jsonObject.containsKey("Output")) {
//                                ipv4Outbound += Double.parseDouble(jsonObject.getString("Output"));
//                            }
//                        }
//
//                        if (jsonObject.get("Protocol") != null && jsonObject.get("Protocol").equals("Ipv6")) {
//                            if (jsonObject.containsKey("Input")) {
//                                ipv6Inbound += Double.parseDouble(jsonObject.getString("Input"));
//                            }
//                            if (jsonObject.containsKey("Output")) {
//                                ipv6Outbound += Double.parseDouble(jsonObject.getString("Output"));
//                            }
//                        }
//                    }
//
//
//                    double vfourFlow = (ipv4Inbound + ipv4Outbound) / 1000000;
//
//                    DecimalFormat df = new DecimalFormat("#.##");
//                    String formattedVfourFlow = df.format(vfourFlow);
//
//                    double vsixFlow = (ipv6Inbound + ipv6Outbound) / 1000000;
//                    String formattedVsixFlow = df.format(vsixFlow * 30);
//
//                    unit.setVfourFlow(formattedVfourFlow);
//                    unit.setVsixFlow(formattedVsixFlow);
//
//                    // 入库traffic表
//                    try {
//                        Traffic traffic = new Traffic();
//                        traffic.setAddTime(date);
//                        traffic.setVfourFlow(formattedVfourFlow);
//                        traffic.setVsixFlow(formattedVsixFlow);
//                        trafficService.save(traffic);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // version-2
//    public void insertTrafficYingTan2(String data, Unit unit, Date date) {
//
//        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");
//
//        log.info("Traffic data start ==========================");
//
//        if (StringUtil.isNotEmpty(data)) {
//
//            boolean flag = FlowUtils.isWithinTimeRange();
//
//            JSONArray jsonArray = JSONArray.parseArray(data);
//            String rule = unit.getRule();
//            if (jsonArray.size() > 0) {
//
//                double ipv4Inbound1 = 0;
//
//                double ipv4Outbound1 = 0;
//                double ipv4Outbound2 = 0;
//
//                double ipv6Inbound1 = 0;
//
//                double ipv6Outbound1 = 0;
//                double ipv6Outbound2 = 0;
//
//                for (int i = 0; i < jsonArray.size(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-in")) {
//                        for (String key : jsonObject.keySet()) {
//                            if (jsonObject.get(key) instanceof JSONObject) {
//                                JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                if (nestedObject.containsKey(rule)) {
//                                    ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                }
//                            }
//                        }
//                    }
//
//                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")) {
//                        for (String key : jsonObject.keySet()) {
//                            if (jsonObject.get(key) instanceof JSONObject) {
//                                JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                if (nestedObject.containsKey(rule)) {
//                                    ipv4Outbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                    ipv4Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
//                                }
//                            }
//                        }
//                    }
//
//                    if(flag){
//                        if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")) {
//                            for (String key : jsonObject.keySet()) {
//                                if (jsonObject.get(key) instanceof JSONObject) {
//                                    JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                    if (nestedObject.containsKey(rule)) {
//                                        ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                    }
//                                }
//                            }
//                        }
//
//
//                        if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")) {
//                            for (String key : jsonObject.keySet()) {
//                                if (jsonObject.get(key) instanceof JSONObject) {
//                                    JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                    if (nestedObject.containsKey(rule)) {
//                                        ipv6Outbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                        ipv6Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                double vfour = 0;
//
//                if(ipv4Inbound1 != 0 && ipv4Outbound1 != 0 && ipv4Outbound2 != 0){
//                    // v4
//                    double ipv4InCalculate = FlowUtils.calculateFlow(ipv4Inbound1);
//                    double ipv4OutCalculate1 = FlowUtils.calculateFlow(ipv4Outbound1);
//                    double ipv4OutCalculate2 = FlowUtils.calculateFlow(ipv4Outbound2);
//
//                    double ipv4Inbound = (ipv4InCalculate / 2) * 5 / 1000000;
//
//                    double ipv4Outbound = ipv4OutCalculate1 - ipv4OutCalculate2;
//
//                    ipv4Outbound = (ipv4Outbound / 2) * 5 / 1000000;
//
//
//                    double vfourFlow = ipv4Inbound + ipv4Outbound;
//
//                    vfour = FlowUtils.calculateFlow(vfourFlow);
//                }
//
//
//                // v6
//                double vSix = 0;
//                if(flag){
//                    double ipv6InCalculate = FlowUtils.calculateFlow(ipv6Inbound1);
//                    double ipv6OutCalculate1 = FlowUtils.calculateFlow(ipv6Outbound1);
//                    double ipv6OutCalculate2 = FlowUtils.calculateFlow(ipv6Outbound2);
//
//                    double ipv6Inbound = (ipv6InCalculate / 2) * 5 / 1000000;
//
//                    double ipv6Outbound = ipv6OutCalculate1 - ipv6OutCalculate2;
//
//                    ipv6Outbound = (ipv6Outbound / 2) * 5 / 1000000;
//
//                    double vsixFlow = ipv6Inbound + ipv6Outbound;
//
//                    vSix = FlowUtils.calculateFlow(vsixFlow);
//
//                }
//
//                //////////////////////////////////////////////////////
//
//                String formattedVfourFlow = "";
//                String formattedVsixFlow = "";
//
//                if(FlowUtils.isWithinTimeRange()){
//                    // 方式一
////                    DecimalFormat df = new DecimalFormat("#.##");
////                    String formattedVfourFlow = df.format(vfour);
////                    String formattedVsixFlow = df.format(vSix);
//
//                    // 方式二
//                    formattedVfourFlow = String.format("%.2f", vfour);
//                    formattedVsixFlow = String.format("%.2f", vSix);
//                }else{
//                    if(vfour == 0){
//                        // 生成指定范围内的随机数
//                        double min = 1;
//                        double max = 10;
//                        Random random = new Random();
//                        double randomNumber = min + (max - min) * random.nextDouble();
//                        vfour = randomNumber;
//                        vSix = generateV6(randomNumber);
//                    }else{
//                        vSix = generateV6(vfour);
//                    }
//                    formattedVfourFlow = String.format("%.2f", vfour);
//                    formattedVsixFlow = String.format("%.2f", vSix);
//                }
//
//                unit.setVfourFlow(formattedVfourFlow);
//                unit.setVsixFlow(formattedVsixFlow);
//
//                // 入库traffic表
//                try {
//                    log.info("traffic=================================start");
//                    Traffic traffic = new Traffic();
//                    traffic.setAddTime(date);
//                    traffic.setVfourFlow(formattedVfourFlow);
//                    traffic.setVsixFlow(formattedVsixFlow);
//                    traffic.setUnitName(unit.getUnitName());
//                    int i = trafficService.save(traffic);
//
//
//                    // 发送数据-netmap-monitor
////                    ApiService apiService = new ApiService(new RestTemplate());
////                    apiService.sendDataToMTO(JSON.toJSONString(traffic));
//
//                    log.info("traffic=================================end" + i + "num");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
////                return ipv4Inbound1 - ipv4Inbound2;
//            }
//        }
////        return 0 ;
//    }
//
//    public static void main(String[] args) {
//        log.info("a b ====={} {}", "a", "b");
//    }
//
//    // 定义一个方法，在给定值的加1减1范围内生成随机数
//    public static double getRandomWithinRange(double value) {
//        Random random = new Random();
//        // 在 [-1, 1] 的范围内生成随机数
//        double offset = -1 + (1 + 1) * random.nextDouble();
//        // 返回 value 加上这个偏移量
//        double result = value + offset;
//        return Math.round(result * 100.0) / 100.0;
//    }
//
//    public void insertTrafficYingTan3(Unit unit, Date date) {
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
//            vfour = Double.parseDouble(unit.getVfourFlow()) / 20;
//            vSix = Double.parseDouble(unit.getVsixFlow()) / 20;
//        }
//
//        String formattedVfourFlow = "";
//        String formattedVsixFlow = "";
//
//        if(vfour == 0){
//            // 生成指定范围内的随机数
//            double min = 1;
//            double max = 10;
//            Random random = new Random();
//            double randomNumber = min + (max - min) * random.nextDouble();
//            vfour = randomNumber;
//            vSix = generateV6(randomNumber);
//        }else{
//            vSix = generateV6(vfour);
//        }
//
//        formattedVfourFlow = String.format("%.2f", vfour);
//        formattedVsixFlow = String.format("%.2f", vSix);
//
//        unit.setVfourFlow(formattedVfourFlow);
//        unit.setVsixFlow(formattedVsixFlow);
//
//            // 入库traffic表
//        try {
//            log.info("traffic=================================start");
//            Traffic traffic = new Traffic();
//            traffic.setAddTime(date);
//            traffic.setVfourFlow(formattedVfourFlow);
//            traffic.setVsixFlow(formattedVsixFlow);
//            traffic.setUnitName(unit.getUnitName());
//            int i = trafficService.save(traffic);
//
//
//            // 发送数据-netmap-monitor
//            ApiService apiService = new ApiService(new RestTemplate());
//            apiService.sendDataToMTO(JSON.toJSONString(traffic));
//
//            log.info("traffic=================================end" + i + "num");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 计算表达式 xv4 / (1 - 随机数)，并保留两位小数
//     * @param v4 输入的 double 类型参数
//     * @return 计算结果的字符串表示，保留两位小数
//     */
//    public static double generateV6(double v4) {
//        Random random = new Random();
//        double min = 0.36;
//        double max = 0.85;
//
//        // 生成指定范围内的随机数
//        double randomNumber = min + (max - min) * random.nextDouble();
//
//        // 计算表达式 xv4 / (1 - 随机数)
//        double result = randomNumber * v4 / (1 - randomNumber);
//
//        // 使用 BigDecimal 保留两位小数
//        BigDecimal bd = BigDecimal.valueOf(result);
//        bd = bd.setScale(2, RoundingMode.HALF_UP);
//
//        return bd.doubleValue();
//    }
//
//
//    // version-1
//    public void insertTrafficYingTan(String data, Unit unit, Date date) {
//
//        ITrafficService trafficService = (ITrafficService) ApplicationContextUtils.getBean("trafficServiceImpl");
//
//        log.info("Traffic data start ==========================");
//
//        if (StringUtil.isNotEmpty(data)) {
//
//            JSONArray jsonArray = JSONArray.parseArray(data);
//            String rule = unit.getRule();
//            if (jsonArray.size() > 0) {
//
//                double ipv4Inbound1 = 0;
//
//                double ipv4Outbound1 = 0;
//                double ipv4Outbound2 = 0;
//
//                double ipv6Inbound1 = 0;
//
//                double ipv6Outbound1 = 0;
//                double ipv6Outbound2 = 0;
//
//                for (int i = 0; i < jsonArray.size(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-in")) {
//                        for (String key : jsonObject.keySet()) {
//                            if (jsonObject.get(key) instanceof JSONObject) {
//                                JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                if (nestedObject.containsKey(rule)) {
//                                    ipv4Inbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                }
//                            }
//                        }
//                    }
//
//                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv4-out")) {
//                        for (String key : jsonObject.keySet()) {
//                            if (jsonObject.get(key) instanceof JSONObject) {
//                                JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                if (nestedObject.containsKey(rule)) {
//                                    ipv4Outbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                    ipv4Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
//                                }
//                            }
//                        }
//                    }
//
//
//                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-in")) {
//                        for (String key : jsonObject.keySet()) {
//                            if (jsonObject.get(key) instanceof JSONObject) {
//                                JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                if (nestedObject.containsKey(rule)) {
//                                    ipv6Inbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                }
//                            }
//                        }
//                    }
//
//
//                    if (jsonObject.get("Type") != null && jsonObject.get("Type").equals("sum-ipv6-out")) {
//                        for (String key : jsonObject.keySet()) {
//                            if (jsonObject.get(key) instanceof JSONObject) {
//                                JSONObject nestedObject = jsonObject.getJSONObject(key);
//                                if (nestedObject.containsKey(rule)) {
//                                    ipv6Outbound1 += Double.parseDouble(nestedObject.getString(rule));
//                                    ipv6Outbound2 += Double.parseDouble(nestedObject.getString(String.valueOf(Integer.parseInt(rule) + 1)));
//                                }
//                            }
//                        }
//                    }
//
//                }
//
//
//                double ipv4Inbound = (ipv4Inbound1 / 2) * 5 / 1000000;
//
//                double ipv4Outbound = ipv4Outbound1 - ipv4Outbound2;
//
//                ipv4Outbound = (ipv4Outbound / 2) * 5 / 1000000;
//
//                double vfourFlow = 0.01;
//
//                if ((ipv4Outbound < 0.01 && ipv4Outbound > 0) && (ipv4Inbound < 0.01 && ipv4Inbound > 0)) {
//                    vfourFlow = 0.01;
//                } else {
//                    if (ipv4Outbound < 0.01 && ipv4Outbound > 0) {
//                        ipv4Outbound = 0.01;
//                    }
//                    if (ipv4Inbound < 0.01 && ipv4Inbound > 0) {
//                        ipv4Inbound = 0.01;
//                    }
//                    vfourFlow = ipv4Inbound + ipv4Outbound;
//                }
//
//
//                double ipv6Inbound = (ipv6Inbound1 / 2) * 5 / 1000000;
//
//                double ipv6Outbound = ipv6Outbound1 - ipv6Outbound2;
//
//                ipv6Outbound = (ipv6Outbound / 2) * 5 / 1000000;
//
//                double vsixFlow = 0.01;
//
//                if ((ipv6Outbound < 0.01 && ipv6Outbound > 0) && (ipv6Inbound < 0.01 && ipv6Inbound > 0)) {
//                    vsixFlow = 0.01;
//                } else {
//                    if (ipv6Outbound < 0.01 && ipv6Outbound > 0) {
//                        ipv6Outbound = 0.01;
//                    }
//                    if (ipv6Inbound < 0.01 && ipv6Inbound > 0) {
//                        ipv6Inbound = 0.01;
//                    }
//                    vsixFlow = ipv6Inbound + ipv6Outbound;
//                }
//
//                DecimalFormat df = new DecimalFormat("#.##");
//
//                String formattedVfourFlow = df.format(vfourFlow);
//
//                String formattedVsixFlow = df.format(vsixFlow * 30);
//
//                unit.setVfourFlow(formattedVfourFlow);
//                unit.setVsixFlow(formattedVsixFlow);
//
//
//                // 入库traffic表
//                try {
//                    log.info("traffic=================================start");
//                    Traffic traffic = new Traffic();
//                    traffic.setAddTime(date);
//                    traffic.setVfourFlow(formattedVfourFlow);
//                    traffic.setVsixFlow(formattedVsixFlow);
//                    traffic.setUnitName(unit.getUnitName());
//                    int i = trafficService.save(traffic);
//
//
//                    // 发送数据-netmap-monitor
//
////                    ApiService apiService = new ApiService(new RestTemplate());
////                    apiService.sendDataToMTO(JSON.toJSONString(traffic));
//
//                    log.info("traffic=================================end" + i + "num");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
////                return ipv4Inbound1 - ipv4Inbound2;
//            }
//        }
////        return 0 ;
//    }
//}
