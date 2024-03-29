package com.metoo.nrsm.core.utils.gather.gathermac;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.IMacService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-23 10:46
 *
 * Java事务注解失效的场景有以下几种：
 *
 * 注解被错误使用：事务注解被错误地应用到非public方法上，或者被应用到一个没有被Spring容器管理的类上，这样会导致注解失效。
 * 异常被处理了：事务注解只在抛出未捕获的异常时才起作用，如果异常被捕获并处理了，事务注解可能会失效。
 * 异常被忽略了：在使用事务注解时，如果在方法中发生了异常但没有被捕获并抛出，或者异常被捕获后没有重新抛出，事务注解可能会失效。
 * 注解的生命周期不正确：事务注解的生命周期必须和Spring容器的生命周期保持一致，如果注解的生命周期不正确，事务注解可能会失效。
 * 配置错误：事务注解的配置可能会出现错误，例如事务的传播行为、隔离级别等配置错误，这样也会导致注解失效。
 * 不支持的事务管理器：某些事务管理器可能不支持某些注解，如果使用了不支持的事务管理器，事务注解可能会失效。
 * 缺少必要的配置：事务注解可能需要一些额外的配置才能正常工作，例如需要配置数据源、事务管理器等，如果缺少了必要的配置，事务注解可能会失效
 * mysql：如使用mysql且引擎是MyISAM，则事务会不起作用，原因是MyISAM不支持事务，可以改成InnoDB引擎
 */
@Component
public class GatherMacUtils {

    @Autowired
    private IMacService macService;
    @Autowired
    private ITerminalService terminalService;

    /**
     *     REQUIRED(0), 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。这是最常见的传播行为，适用于大部分情况。
     *     SUPPORTS(1),如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式执行。这种传播行为适用于不需要事务支持的方法。
     *     MANDATORY(2),如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。这种传播行为适用于必须在事务中执行的方法
     *     REQUIRES_NEW(3),无论当前是否存在事务，都创建一个新的事务。如果当前存在事务，则将当前事务挂起。这种传播行为适用于需要独立事务的方法。
     *     NOT_SUPPORTED(4),以非事务的方式执行方法。如果当前存在事务，则将当前事务挂起。这种传播行为适用于不需要事务支持的方法。
     *     NEVER(5),以非事务的方式执行方法。如果当前存在事务，则抛出异常。这种传播行为适用于不允许在事务中执行的方法。
     *     NESTED(6);如果当前存在事务，则在嵌套事务中执行；如果当前没有事务，则创建一个新的事务。嵌套事务是保存在当前事务中的子事务，它可以独立于父事务进行提交或回滚。这种传播行为适用于需要嵌套事务支持的方法。
     *     以上是Java中事务的七种传播行为，根据具体的业务需求和场景选择合适的传播行为可以有效地管理事务。
     */
    // 复制采集数据到Mac表
    @Transactional(propagation = Propagation.REQUIRED)
    public void copyGatherData(Date date){
        try {
            this.macService.copyGatherDataToMac();

            this.mac_dt();



        } catch (Exception e) {
            e.printStackTrace();

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }


    // 如何让这两个操作在同一个事务中（什么是事务传播 ）
    @Transactional(propagation = Propagation.REQUIRED)
    public void testTransaction(){

        this.macService.deleteTable();

        this.macService.copyGather();

    }



    /**
     * mac对端设备
     * @param e
     * @param lldps
     * @param hostname
     * @param date
     */
    public void setRemoteDevice(NetworkElement e, List<Map> lldps, String hostname, Date date){
        // 写入对端信息
        if(lldps != null && lldps.size() > 0){
            for(Map<String, String> obj : lldps){
                Mac mac = new Mac();
                mac.setAddTime(date);
                mac.setDeviceIp(e.getIp());
                mac.setDeviceName(e.getDeviceName());
//                mac.setPort(e.getPort());
                mac.setMac("00:00:00:00:00:00");
                mac.setHostname(hostname);
                mac.setTag("DE");
                mac.setRemotePort(obj.get("remoteport"));
                mac.setRemoteDevice(obj.get("hostname"));
                this.macService.save(mac);
            }
        }
    }

    // Mac-DT
    public void mac_dt(){

        // 读取mac表，与up接口的mac不重复标记为X，0:0:5e:0标记为V
        this.tag_x();
        this.tag_u();
        this.tag_s();
        this.tag_STOE();

        this.tag_STORT();

//        this.copyArpMacAndIpToMac()
// ;
        this.tag_XToE();
        this.tag_UToE();
//        this.selectXToUTByMap();
        this.selectUToRT();
//        this.RTToDT();
        this.RTToDT2();

        this.copyArpIpToMacByDT();

    }

    // 读取mac表，与up接口的mac不重复标记为X，0:0:5e:0标记为V
//    public void tag_x(){// 全部设备
//        Map params = new HashMap();
//        params.put("tags", Arrays.asList("L", "LV"));
//        List<Mac> macs = this.macService.selectObjByMap(params);
//        if(macs.size() > 0){
//            Set<String> macSet = new HashSet<>();
//            macs.forEach(e -> {
//                macSet.add(e.getMac());
//            });
//
//            params.clear();
//            params.put("notMacSet", macSet);
//            params.put("tagIsNull", "tagIsNull");
////            params.put("notTag", "DE");
//            List<Mac> unequalToUpMac = this.macService.selectObjByMap(params);
//            if(unequalToUpMac.size() > 0){
//                unequalToUpMac.stream().forEach(e -> e.setTag(setTag("X", e.getMac())));
////                for (Mac mac : unequalToUpMac) {
////                    mac.setTag(setTag("X", mac.getMac()));
////                }
//                this.macService.batchUpdate(unequalToUpMac);
//            }
//        }
//    }

    // 标记为X
    public void tag_x(){// 单台设备
        List<Mac> unequalToUpMac = this.macService.selectTagToX(null);
        if(unequalToUpMac.size() > 0){
            unequalToUpMac.stream().forEach(e -> e.setTag(setTag("X", e.getMac())));
            this.macService.batchUpdate(unequalToUpMac);
        }
    }

    // 根据mac地址，标记V
    public String setTag(String tag, String mac){
        String patten = "^" + "00:00:5e";
        boolean flag = this.parseLineBeginWith(mac, patten);
        if (flag) {
            return "V";
        }
        return tag;
    }

    /**
     * 判断Mac是否以某个规则开始
     * @param lineText
     * @param head
     * @return
     */
    public boolean parseLineBeginWith(String lineText, String head){

        if(StringUtil.isNotEmpty(lineText) && StringUtil.isNotEmpty(head)){
            String patten = "^" + head;

            Pattern compiledPattern = Pattern.compile(patten);

            Matcher matcher = compiledPattern.matcher(lineText);

            while(matcher.find()) {
                return true;
            }
        }
        return false;
    }

    // 标记U(1个mac对应1个port(除去L之外)，此条目标记为U)
    public void tag_u(){// 单台设备
        List<Mac> macs = this.macService.selectTagToU(null);
        if(macs.size() > 0){
            macs.stream().forEach(e -> e.setTag("U"));
            this.macService.batchUpdate(macs);
        }
    }

    public void tag_s(){// 单台设备
        List<Mac> macs = this.macService.selectTagToS(null);
        if(macs.size() > 0){
            macs.stream().forEach(e -> e.setTag("S"));
            this.macService.batchUpdate(macs);
        }
    }

    public void tag_STOE(){// 单台设备
        List<Mac> macs = this.macService.selectTagSToE(null);
        if(macs.size() > 0){
            macs.stream().forEach(e -> e.setTag("E"));
            this.macService.batchUpdate(macs);
        }
    }

    public void tag_STORT(){// 单台设备
        List<Mac> macs = this.macService.selectTagSToRT(null);
        if(macs.size() > 0){
            macs.stream().forEach(e -> e.setTag("RT"));
            this.macService.batchUpdate(macs);
        }
    }

    // 将arp表中mac对应的ip地址、mac厂商写入mac表(不包含DE)
    public void copyArpMacAndIpToMac(){
        try {
            List<Mac> macs = this.macService.copyArpMacAndIpToMac(null);
            if(macs != null && macs.size() > 0){
//                for (Mac mac : macs) {
//                    this.macService.update(mac);
//                }
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 为x的条目如果在全网(除本机)匹配到任何1条为L的标记，则此条目标记为E(Equipment)
    public void tag_XToE(){
        try {
            List<Mac> macs = this.macService.selectXToEByMap(null);
            if(macs != null && macs.size() > 0){
                macs.stream().forEach(e -> e.setTag("E"));
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tag_UToE(){
        try {
            List<Mac> macs = this.macService.selectUToEByMap(null);
            if(macs != null && macs.size() > 0){
                macs.stream().forEach(e -> e.setTag("E"));
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectUToRT(){
        try {
            List<Mac> macs = this.macService.selectUToRTByMap(null);
            if(macs != null && macs.size() > 0){
                Set set1 = macs.stream().map(e -> e.getId()).collect(Collectors.toSet());
                Set set2 = macs.stream().flatMap(e -> e.getMacList().stream()).map(e -> e).collect(Collectors.toSet());
                set1.addAll(set2);
                this.macService.updateMacTagToRTByIds(set1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 标记为UT且有ip地址的，标记为DT
    public void RTToDT(){
        try {
            List<Mac> macs = this.macService.selectRTToDTByMap(null);
            if(macs != null && macs.size() > 0){
                Set set1 = macs.stream().map(e -> e.getId()).collect(Collectors.toSet());
                this.macService.updateMacTagToDTByIds(set1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RTToDT2(){
        try {
            List<Mac> macs = this.macService.selectRTToDT2ByMap(null);
            if(macs != null && macs.size() > 0){
                Set set1 = macs.stream().map(e -> e.getId()).collect(Collectors.toSet());
                this.macService.updateMacTagToDTByIds(set1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyArpIpToMacByDT(){
        try {
            List<Mac> macs = this.macService.copyArpIpToMacByDT(null);
            if(macs != null && macs.size() > 0){
//                for (Mac mac : macs) {
//                    this.macService.update(mac);
//                }
                this.macService.batchUpdate(macs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
