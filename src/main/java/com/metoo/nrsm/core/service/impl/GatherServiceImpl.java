package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMultithreadingMacUtils;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherSingleThreadingMacUtils;
import com.metoo.nrsm.core.utils.gather.thread.*;
import com.metoo.nrsm.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 10:29
 */

@Service
public class GatherServiceImpl implements IGatherService {

    @Autowired
    private IArpService arpService;
    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private Ipv6Service ipv6Service;
    @Autowired
    private INetworkElementService networkElementService;

    @Autowired
    private GatherSingleThreadingMacUtils gatherSingleThreadingMacUtils;
    @Autowired
    private GatherMultithreadingMacUtils gatherMultithreadingMacUtils;

    @Override
    public void gatherMac(Date date) {
        gatherSingleThreadingMacUtils.gatherMac(date);
    }

    @Override
    public void gatherMacThread(Date date) {
        this.gatherMultithreadingMacUtils.gatherMacThread(date);
    }

    @Override
    public void gatherArp(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
//            // 步骤一 清空采集表
//            this.arpService.truncateTableGather();
//            // 清空数据之后的插入失败（测试清空表后，等待3秒）
////            try {
////                Thread.sleep(3000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//
////            // 步骤二 合并mac和port相同的数据（ipv4和ipv6）到arp表
////            this.mergeIpv4AndIpv6ToArpGather_sql_insert(date);
////
////            // 步骤三 排除上个步骤中的数据，写入arp表
////            this.arpService.writeArp();
//
//            // 步骤 二和三合并
//            this.batchSaveIpV4AndIpv6ToArpGather(date);
//
//            // 步骤四 复制数据到arp表（待测试：使用delete 或 truncate ）
//            this.copyGatherDataToArp();

            // 合并一二三四步骤（以上步骤可完成arp数据，bug：自动采集时没有ipv6需等待N秒）
            // 合并后使用存储过程（性能待测试）
            this.arpService.gatherArp(date);
        }
    }

    public void mergeIpv4AndIpv6ToArpGather_sql(Date date){
        Map params = new HashMap();
        params.put("addTime", date);
        List<Arp> arps = this.arpService.mergeIpv4AndIpv6(params);
        if(arps.size() > 0){
//            arps.stream().forEach(e -> e.setAddTime(date));
            this.arpService.batchSaveGather(arps);
        }
    }

    public void mergeIpv4AndIpv6ToArpGather_sql_insert(Date date){
        Map params = new HashMap();
        params.put("addTime", date);
        try {
            this.arpService.batchSaveGatherBySelect(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mergeIpv4AndIpv6ToArpGather_code(Date date){
        List<Arp> arps = this.arpService.joinSelectObjAndIpv6();
        if(arps.size() > 0) {
            for (Arp arp : arps) {
                arp.setAddTime(date);
                List<Ipv6> ipv6s = arp.getIpv6List();
                if (ipv6s.size() > 0) {
                    if (ipv6s.size() == 1) {
                        arp.setV6ip(ipv6s.get(0).getIp());
                    } else {
                        for (int i = 0; i < ipv6s.size(); i++) {
                            String v6ip = "v6ip";
                            if(i > 0){
                                v6ip = "v6ip" + i;
                            }
                            Field[] fields = Arp.class.getDeclaredFields();
                            for (Field field : fields) {
                                String propertyName = field.getName(); // 获取属性名

                                // 设置属性值为"Hello World!"
                                if (v6ip.equalsIgnoreCase(propertyName)) {
                                    field.setAccessible(true); // 若属性为private或protected需要先调用此方法进行访问控制的关闭
                                    try {
                                        field.set(arp, ipv6s.get(i).getIp());
                                        break;
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
//                this.arpService.saveGather(arp);
            }
            this.arpService.batchSaveGather(arps);
        }
    }

    public void batchSaveIpV4AndIpv6ToArpGather(Date date){
        Map params = new HashMap();
        params.put("addTime", date);
        this.arpService.batchSaveIpV4AndIpv6ToArpGather(params);
    }

    // 复制采集数据到ipv4表
    public void copyGatherDataToArp(){
        try {
            this.arpService.copyGatherDataToArp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gatherIpv4(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            this.ipv4Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "getarp.py";
//                String result = PythonExecUtils.exec(path);
                String[] params = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = PythonExecUtils.exec(path, params);
                if(!"".equals(result)){
                    try {
                        List<Ipv4> ipv4s = JSONObject.parseArray(result, Ipv4.class);
                        if(ipv4s.size()>0){
                            ipv4s.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                e.setAddTime(date);
//                                this.ipv4Service.saveGather(e);
                            });
                            this.ipv4Service.batchSaveGather(ipv4s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 非多线程，单线程串行情况下可放到最后执行(提到前面？)
            this.copyGatherDataToIpv4();
            this.removeDuplicatesIpv4();
        }
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void gatherIpv4Thread(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            // （采集结束，复制到ipv4表中）
            // 多线程并行执行，避免出现采集未结束时，执行copy操作没有数据，所以将复制数据操作，放到采集前，复制上次采集结果即可
            this.copyGatherDataToIpv4();

            this.removeDuplicatesIpv4();

            this.ipv4Service.truncateTableGather();

            for (NetworkElement networkElement : networkElements) {
//                Thread thread = new Thread(new GatherIpV4Runnable(networkElement, date));
//                thread.start();
                GatherDataThreadPool.getInstance().addThread(new GatherIpV4Runnable(networkElement, date));
            }
        }
    }

    // 复制采集数据到ipv4表
    public void copyGatherDataToIpv4(){
        try {
//            this.ipv4Service.truncateTable();
            this.ipv4Service.copyGatherToIpv4();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 去重ipv4重复数据到ipv4_mirror(组合arp表)
    public void removeDuplicatesIpv4(){
        try {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.ipv4Service.removeDuplicates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void gatherIpv6(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            this.ipv6Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                String path = Global.PYPATH + "getarp.py";
                path = Global.PYPATH +  "getarpv6.py";
//                result = PythonExecUtils.exec(path);
                String[] params2 = {networkElement.getIp(), networkElement.getVersion(),
                        networkElement.getCommunity()};
                String result = PythonExecUtils.exec(path, params2);
                if(!"".equals(result)){
                    try {
                        List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
                        if(array.size()>0){
                            array.forEach(e -> {
                                e.setDeviceIp(networkElement.getIp());
                                e.setDeviceName(networkElement.getDeviceName());
                                e.setAddTime(date);
//                                this.ipv6Service.saveGather(e);
                            });
                        }
                        this.ipv6Service.batchSaveGather(array);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // 非多线程，单线程串行情况下可放到最后执行
            this.copyGatherDataToIpv6();
            this.removeDuplicatesIpv6();
        }
    }


    @Override
    public void gatherIpv6Thread(Date date) {
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {
            // 非多线程，单线程串行情况下可放到最后执行
            this.copyGatherDataToIpv6();

            this.removeDuplicatesIpv6();

            this.ipv6Service.truncateTableGather();
            for (NetworkElement networkElement : networkElements) {
                // 不要显式创建线程，请使用线程池
                /** 线程资源必须通过线程池提供，不允许在应用中自行显式创建线程。
                    说明：使用线程池的好处是减少在创建和销毁线程上所花的时间以及系统资源的开销，解决资源不足的问题。
                    如果不使用线程池，有可能造成系统创建大量同类线程而导致消耗完内存或者“过度切换”的问题
                 **/
//                Thread thread = new Thread(new GatherIpV6Runnable(networkElement, date));
//                thread.start();
                GatherDataThreadPool.getInstance().addThread(new GatherIpV6Runnable(networkElement, date));

            }
        }
    }

    // 复制采集数据到ipv6表
    public void copyGatherDataToIpv6(){
        try {
            this.ipv6Service.copyGatherToIpv6();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 去重ipv4重复数据，组合arp表
    public void removeDuplicatesIpv6(){
        try {
            // 去重（将采集到的数据，复制并创创建相同结构的表中，并去除重复数据（mac + ip），使用存储过程完成）
            this.ipv6Service.removeDuplicates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private IPortService portService;

    /**
     * 获取设备端口
     */
    @Override
    public void gatherPort(Date date){
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {

            this.portService.copyGatherData();

            this.portService.truncateTableGather();

            for (NetworkElement networkElement : networkElements) {

                GatherDataThreadPool.getInstance().addThread(new GatherPortRunnable(networkElement, date));

            }
        }
    }

    @Autowired
    private IPortIpv6Service portIpv6Service;

    @Override
    public void gatherPortIpv6(Date date){
        List<NetworkElement> networkElements = this.networkElementService.selectObjAll();
        if(networkElements.size() > 0) {

            this.portIpv6Service.copyGatherData();

            this.portIpv6Service.truncateTableGather();

            for (NetworkElement networkElement : networkElements) {

                GatherDataThreadPool.getInstance().addThread(new GatherPortIpv6Runnable(networkElement, date));

            }
        }
    }


}
