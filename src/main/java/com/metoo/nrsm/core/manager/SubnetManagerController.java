package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.utils.RsmsDeviceUtils;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api("子网管理")
@RequestMapping("/admin/subnet")
@RestController
public class SubnetManagerController {

    @Autowired
    private ISubnetService subnetService;
    @Autowired
    private IPortService portService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private Ipv4DetailService ipV4DetailService;
    @Autowired
    private IRsmsDeviceService rsmsDeviceService;
    @Autowired
    private RsmsDeviceUtils rsmsDeviceUtils;

    @RequestMapping("/list")
    public Object list() {
        // 获取所有子网一级
        List<Subnet> parentList = this.subnetService.selectSubnetByParentId(null);
        if (parentList.size() > 0) {
            for (Subnet subnet : parentList) {
                this.genericSubnet(subnet);
            }
            return ResponseUtil.ok(parentList);
        }
        return ResponseUtil.ok();
    }

//    @ApiOperation("根据网段id查询直接从属子网")
//    @GetMapping(value = {"", "/{id}"})
//    public Object getSubnet(@PathVariable(value = "id", required = false) Long id) {
//        if (id == null) {
//            // 获取所有子网一级
//            List<Subnet> parentList = this.subnetService.selectSubnetByParentId(null);
//            if (parentList.size() > 0) {
//                for (Subnet subnet : parentList) {
//                    this.genericSubnet(subnet);
//                }
//                return ResponseUtil.ok(parentList);
//            }
//        } else {
//            // 校验子网是否存在
//            Subnet subnet = this.subnetService.selectObjById(id);
//            if (subnet != null) {
//                // 当前网段
//                Map map = new HashMap();
//                map.put("subnet", subnet);
//                // 获取从子网列表
//                List<Subnet> subnetList = this.subnetService.selectSubnetByParentId(id);
//                //
//                map.put("subnets", subnetList);
//                // 查询IP addresses in subnets
//                if (subnetList.size() <= 0 && subnet.getMask() >= 24) {
//                    // 获取地址列表
//                    // 获取最大Ip地址和最小Ip地址
//                    String mask = Ipv4Util.bitMaskConvertMask(subnet.getMask());
//                    Map networkMap = Ipv4Util.getNetworkIp(subnet.getIp(), mask);
//                    String[] ips = Ipv4Util.getSubnetList(networkMap.get("network").toString(),
//                            subnet.getMask());
////                    if (ips.length > 0) {
////                        Map addresses = new LinkedHashMap();
////                        for (String ip : ips) {
////                            Address address = this.addressService.selectObjByIp(IpUtil.ipConvertDec(ip));
////                            if (address != null) {
////                                IpDetail ipDetail = this.ipDetailService.selectObjByIp(IpUtil.ipConvertDec(ip));
////                                if (ipDetail != null) {
////                                    int time = ipDetail.getTime();
////                                    // 每分钟采一次
////                                    int hourAll = time / 60;// 一共多少小时
////                                    int day = hourAll / 24;
////                                    int hour = hourAll % 24;
////                                    ipDetail.setDuration(day + "天" + hour + "小时");
////                                    address.setIpDetail(ipDetail);
////                                }
////                                // 写入Ip地址的设备信息
////                                Map deviceInfo = this.rsmsDeviceUtils.getDeviceInfo(address.getIp());
////                                address.setDeviceInfo(deviceInfo);
////                            }
////                            addresses.put(ip, address);
////                        }
////                        map.put("addresses", addresses);
////                    }
//                } else if (subnetList.size() <= 0 && subnet.getMask() < 24 && subnet.getMask() >= 16) {
//                    // 获取网段数量
//                    String mask = Ipv4Util.bitMaskConvertMask(subnet.getMask());
//                    Map networkMap = Ipv4Util.getNetworkIp(subnet.getIp(), mask);
//                    String[] ips = Ipv4Util.getSubnetList(networkMap.get("network").toString(),
//                            subnet.getMask());
//                    int sum = ips.length / 255;
//                    List list = new ArrayList();
//                    if (sum > 0) {
//                        for (int i = 0; i < sum; i++) {
//                            String ip = subnet.getIp();
//                            String[] seg = ip.split("\\.");
//                            StringBuffer buffer = new StringBuffer();
//                            buffer.append(seg[0]).append(".").append(seg[1]).append(".").append(i).append(".").append(seg[3]).append("-").append("255");
//                            list.add(buffer);
//                        }
//                    }
//                    map.put("segmentation", list);
//                } else {
//                    // 查询子网ip地址列表
////                    Map params = new HashMap();
////                    params.put("subnetId", subnet.getId());
////                    List<Address> address = this.addressService.selectObjByMap(params);
////                    map.put("mastSubnetAddress", address);
//                }
//                return ResponseUtil.ok(map);
//            }
//            return ResponseUtil.badArgument("网段不存在");
//        }
//        return ResponseUtil.ok();
//    }

    public List<Subnet> genericSubnet(Subnet subnet) {
        List<Subnet> subnets = this.subnetService.selectSubnetByParentId(subnet.getId());
        if (subnets.size() > 0) {
            for (Subnet child : subnets) {
                List<Subnet> subnetList = genericSubnet(child);
                if (subnetList.size() > 0) {
                    child.setSubnetList(subnetList);
                }
            }
            subnet.setSubnetList(subnets);
        }
        return subnets;
    }

//    @RequestMapping(value = {"/comb"})
//    public Object comb() {
//
//        this.subnetService.deleteTable();
//
//        Map params = new HashMap();
//        params.put("ipIsNotNull", "is not null");
//        params.put("NotIp", "127.0.0.1");
//        List<Port> ports = this.portService.selectObjByMap(params);
//        Map<String, List<Object>> map = this.ipAddressCombingByDB(ports);
//        for (String key : map.keySet()) {
//            int index = key.indexOf("/");
//            String firstIp = key.substring(0, index);
//            int firstMask = Integer.parseInt(key.substring(index + 1));
//            // 插入本地数据库 and 同步ipam
//            // 插如一级ip网段
//            Long firstSubnetId = null;
//            Subnet firstSubnet = this.subnetService.selectObjByIpAndMask(firstIp, firstMask);
//            if (firstSubnet != null) {
//                firstSubnetId = firstSubnet.getId();
//            }
//            if (firstSubnet == null) {
//                Subnet subnet = new Subnet();
//                subnet.setIp(Ipv4Util.ipConvertDec(firstIp));
//                subnet.setMask(firstMask);
//                this.subnetService.save(subnet);
//                firstSubnetId = subnet.getId();
//            }
//            // 获取二级网段
//            JSONArray array = JSONArray.parseArray(JSON.toJSONString(map.get(key)));
//            for (Object obj : array) {
//                if (obj instanceof String) {
//                    String second = ((String) obj).trim();
//                    int sequence = second.indexOf("/");
//                    String ip = second.substring(0, sequence);
//                    int secondMask = Integer.parseInt(second.substring(sequence + 1));
//                    Subnet secondSubnet = this.subnetService.selectObjByIpAndMask(ip, secondMask);
//                    if (secondSubnet == null) {
//                        Subnet subnet = new Subnet();
//                        subnet.setIp(Ipv4Util.ipConvertDec(ip));
//                        subnet.setMask(secondMask);
//                        subnet.setParentIp(Ipv4Util.ipConvertDec(firstIp));
//                        subnet.setParentId(firstSubnetId);
//                        this.subnetService.save(subnet);
//                    }
//                }
//                if (obj instanceof JSONObject) {
//                    JSONObject object = (JSONObject) obj;
//                    for (String okey : object.keySet()) {
//                        String second = okey.trim();
//                        int sequence = second.indexOf("/");
//                        String secondIp = second.substring(0, sequence);
//                        int secondMask = Integer.parseInt(second.substring(sequence + 1));
//                        Long secondSubnetId = null;
//                        Subnet secondSubnet = this.subnetService.selectObjByIpAndMask(secondIp, secondMask);
//                        if (secondSubnet != null) {
//                            secondSubnetId = secondSubnet.getId();
//                        }
//                        if (secondSubnet == null) {
//                            Subnet subnet = new Subnet();
//                            subnet.setIp(Ipv4Util.ipConvertDec(secondIp));
//                            subnet.setMask(secondMask);
//                            subnet.setParentIp(Ipv4Util.ipConvertDec(firstIp));
//                            subnet.setParentId(firstSubnetId);
//                            this.subnetService.save(subnet);
//                            secondSubnetId = subnet.getId();
//                        }
//                        JSONArray thirdArray = JSONArray.parseArray(object.get(okey).toString());
//                        for (Object thirdKey : thirdArray) {
//                            if (obj instanceof JSONObject) {
//                                String third = ((String) thirdKey).trim();
//                                int thirdSequence = third.indexOf("/");
//                                String thirdIp = third.substring(0, thirdSequence);
//                                int thirdMask = Integer.parseInt(third.substring(thirdSequence + 1));
//                                Subnet thirdSubnet = this.subnetService.selectObjByIpAndMask(thirdIp, thirdMask);
//                                if (thirdSubnet == null) {
//                                    Subnet subnet = new Subnet();
//                                    subnet.setIp(Ipv4Util.ipConvertDec(thirdIp));
//                                    subnet.setMask(thirdMask);
//                                    subnet.setParentIp(Ipv4Util.ipConvertDec(secondIp));
//                                    subnet.setParentId(secondSubnetId);
//                                    this.subnetService.save(subnet);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return ResponseUtil.ok();
//    }

    @ApiOperation("删除网段")
    @DeleteMapping
    public Object delete(@RequestParam(value = "id") Long id) {
        Subnet subnet = this.subnetService.selectObjById(id);
        if (subnet != null) {
            // 查询子网ip地址列表
            Map params = new HashMap();
            params.put("subnetId", subnet.getId());
            List<Address> address = this.addressService.selectObjByMap(params);
            for (Address obj : address) {
                this.addressService.delete(obj.getId());
            }
//            // 递归删除所有ip
            try {
                this.genericDel(subnet);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            List<Subnet> subnets = this.subnetService.selectSubnetByParentId(subnet.getId());
//            for (Subnet obj : subnets){
//                params.clear();
//                params.put("subnetId", obj.getId());
//                List<Address> addresses = this.addressService.selectObjByMap(params);
//                for (Address address1 : addresses){
//                    this.addressService.delete(address1.getId());
//                }
//                this.subnetService.delete(obj.getId());
//            }
            // 批量
//            if(subnet != null){
//                this.genericSubnet(subnet);
//            }
//            this.subnetService.delete(id);
        }
        return ResponseUtil.ok();
    }

    public void genericDel(Subnet subnet) {
        List<Subnet> childs = this.subnetService.selectSubnetByParentId(subnet.getId());
        if (childs.size() > 0) {
            for (Subnet child : childs) {
                genericDel(child);
            }
        }
        Map params = new HashMap();
        params.clear();
        params.put("subnetId", subnet.getId());
        List<Address> addresses = this.addressService.selectObjByMap(params);
        for (Address address : addresses) {
            this.addressService.delete(address.getId());
        }
        this.subnetService.delete(subnet.getId());
    }

    @RequestMapping(value = {"/comb"})
    public Object comb() {
        return this.subnetService.comb();
    }

    public Map<String, List<Object>> ipAddressCombingByDB(List<Port> ports) {
        if(ports.size() == 0){
            return new HashMap<>();
        }
        Map<String, Integer> map = new HashMap();
        List<Integer> masks = new ArrayList();
        for (Port port : ports){
            String ip = port.getIp();
            String mask = port.getMask();
            Integer maskBit = Ipv4Util.getMaskBitByMask(mask);
            Map networkMap = Ipv4Util.getNetworkIp(ip, mask);
            map.put(networkMap.get("network").toString(), maskBit);
            masks.add(maskBit);
        }
        // 第二步：提取最短掩码，生成上级网段
        HashSet set = new HashSet(masks);
        masks.clear();
        masks.addAll(set);
        Collections.sort(masks);
        Integer firstMask = masks.get(0);// 最短掩码
        Map<String, Integer> firstMap = new HashMap();
        Map<String, Integer> otherMap = new HashMap();
        for (Map.Entry<String, Integer> entry : map.entrySet()){
            if(entry.getValue().equals(firstMask)){
                firstMap.put(entry.getKey(), entry.getValue());
            }else{
                otherMap.put(entry.getKey(), entry.getValue());
            }
        }
        // 提取
        Map<String, List<Object>> parentMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : firstMap.entrySet()) {
            Integer maskBit = entry.getValue();
            String ip = entry.getKey();
            Integer parentMask = null;
            if (maskBit > 24) {
                parentMask = 24;
            } else if (24 >= maskBit && maskBit > 16) {
                parentMask = 16;
            } else if (16 >= maskBit && maskBit > 8) {
                parentMask = 8;
            }else if(maskBit <= 8){
                parentMask = maskBit;
            }
            String segment = this.getParentSegment(ip, parentMask);// 生成网段
            String parentSegment = segment + "/" + parentMask;
            if (!parentMap.keySet().contains(parentSegment) && parentMap.get(parentSegment) == null) {
                List<Object> childList = new ArrayList<>();
                childList.add(ip + "/" + maskBit);
                parentMap.put(parentSegment, childList);
            } else {
                List<Object> childList = parentMap.get(parentSegment);
                if(childList != null){
                    childList.add(ip + "/" + maskBit);
                }
            }
        }
        // 遍历
        Map parentSegmentMap = new HashMap();
        for (Map.Entry entry : parentMap.entrySet()) {
            String parentSegment = (String) entry.getKey();
            Integer parentMask = null;
            parentMask = Integer.parseInt(parentSegment.substring(parentSegment.indexOf("/") + 1));
            int parentIndex = 0;
            String parentIpPartial = null;
            if (parentMask == 24) {
                parentIndex =  parentSegment.indexOf(".");
                parentIndex =  parentSegment.indexOf(".", parentIndex + 1);
                parentIndex =  parentSegment.indexOf(".", parentIndex + 1);
            } else if (parentMask == 16) {
                parentIndex =  parentSegment.indexOf(".");
                parentIndex =  parentSegment.indexOf(".", parentIndex + 1);
            } else if (parentMask == 8) {
                parentIndex =  parentSegment.indexOf(".");
            }
            parentIpPartial = parentSegment.substring(0, parentIndex);
            parentSegmentMap.put(parentIpPartial, parentSegment);
        }

        // 判断是否属于第一级
        for (Map.Entry<String, Integer> entry : otherMap.entrySet()) {
            Integer mask = entry.getValue();
            String ip = entry.getKey();
            int index = 0;
            if (mask > 24) {
                index =  ip.indexOf(".");
                index =  ip.indexOf(".", index + 1);
                index =  ip.indexOf(".", index + 1);
            } else if (24 >= mask && mask > 16) {
                index =  ip.indexOf(".");
                index =  ip.indexOf(".", index + 1);
            } else if (16 >= mask && mask > 8) {
                index =  ip.indexOf(".");
            }
            String ipParentIpPartial = ip.substring(0, index);
            if(parentSegmentMap.get(ipParentIpPartial) != null){
                List<Object> list = parentMap.get(parentSegmentMap.get(ipParentIpPartial));
                list.add(ip + "/" + mask);
            }else{
                Integer parentMask = null;
                if (mask > 24) {
                    parentMask = 24;
                } else if (24 >= mask && mask > 16) {
                    parentMask = 16;
                } else if (16 >= mask && mask > 8) {
                    parentMask = 8;
                }
                String parentIp = this.getParentIp(ip, parentMask);
                String parentSegment = parentIp + "/" + parentMask;
                List<Object> list = new ArrayList<>();
                list.add(ip + "/" + mask);
                parentMap.put(parentSegment, list);
                parentSegmentMap.put(ipParentIpPartial, parentIp);
            }
        }
        // 遍历二级ip，生成上级Ip
        if(parentMap.size() > 1){
            Map<String, List<Object>> parent = this.getShortMask(parentMap);
            if(parent != null && parent.size() > 0){
                return parent;
            }
        }else{}
        return parentMap;
    }

    public String getParentSegment(String ip, Integer bitmask){
        String segment = "";
        if (24 == bitmask) {
            String mask = Ipv4Util.bitMaskConvertMask(bitmask);
            segment = Ipv4Util.getNetwork(ip, mask);
        } else if (16  == bitmask) {
            String mask = Ipv4Util.bitMaskConvertMask(bitmask);
            segment = Ipv4Util.getNetwork(ip, mask);
        }else if (8  == bitmask) {
            String mask = Ipv4Util.bitMaskConvertMask(bitmask);
            segment = Ipv4Util.getNetwork(ip, mask);
        }
        return segment;
    }
//
//    public Map<String, List<Object>> ipAddressCombingByDB(List<Port> ports) {
//        if(ports.size() == 0){
//            return new HashMap<>();
//        }
//        Map<String, Integer> map = new HashMap();
//        List<Integer> masks = new ArrayList();
//        for (Port port : ports){
//            String ip = port.getIp();
//            String mask = port.getMask();
//            Integer maskBit = IpV4Util.getMaskBitByMask(mask);
//            Map networkMap = IpV4Util.getNetworkIp(ip, mask);
//            map.put(networkMap.get("network").toString(), maskBit);
//            masks.add(maskBit);
//        }
//        // 第二步：提取最短掩码，生成上级网段
//        HashSet set = new HashSet(masks);
//        masks.clear();
//        masks.addAll(set);
//        Collections.sort(masks);
//        Integer firstMask = masks.get(0);// 最短掩码
//        Map<String, Integer> firstMap = new HashMap();
//        Map<String, Integer> otherMap = new HashMap();
//        for (Map.Entry<String, Integer> entry : map.entrySet()){
//            if(entry.getValue().equals(firstMask)){
//                firstMap.put(entry.getKey(), entry.getValue());
//            }else{
//                otherMap.put(entry.getKey(), entry.getValue());
//            }
//        }
//        // 提取
//        Map<String, List<Object>> parentMap = new HashMap<>();
//        for (Map.Entry<String, Integer> entry : firstMap.entrySet()) {
//            Integer maskBit = entry.getValue();
//            String ip = entry.getKey();
//            Integer parentMask = null;
//            if (maskBit > 24) {
//                parentMask = 24;
//            } else if (24 >= maskBit && maskBit > 16) {
//                parentMask = 16;
//            } else if (16 >= maskBit && maskBit > 8) {
//                parentMask = 8;
//            }else if(maskBit <= 8){
//                parentMask = maskBit;
//            }
//            String parentIp = this.getParentIp(ip, parentMask);// 生成网段
//            parentIp = parentIp + "/" + parentMask;
//            if (!parentMap.keySet().contains(parentIp) && parentMap.get(parentIp) == null) {
//                List<Object> childList = new ArrayList<>();
//                childList.add(ip + "/" + maskBit);
//                parentMap.put(parentIp, childList);
//            } else {
//                List<Object> childList = parentMap.get(parentIp);
//                if(childList != null){
//                    childList.add(ip + "/" + maskBit);
//                }
//            }
//
//            String segment = this.getParentIp(ip, parentMask);// 生成网段
//            String parentSegment = segment + "/" + parentMask;
//            if (!parentMap.keySet().contains(parentSegment) && parentMap.get(parentSegment) == null) {
//                List<Object> childList = new ArrayList<>();
//                childList.add(ip + "/" + maskBit);
//                parentMap.put(parentSegment, childList);
//            } else {
//                List<Object> childList = parentMap.get(parentSegment);
//                if(childList != null){
//                    childList.add(ip + "/" + maskBit);
//                }
//            }
//        }
////        遍历
//        Map parentSegment = new HashMap();
//        for (Map.Entry entry1 : parentMap.entrySet()) {
//            String parentIpMask = (String) entry1.getKey();
//            Integer parentMask = null;
//            int sequence = parentIpMask.indexOf("/");
//            parentMask = Integer.parseInt(parentIpMask.substring(sequence + 1));
//            int parentIndex = 0;
//            String parentIpSeggment = null;
//            if (parentMask == 24) {
//                parentIndex =  parentIpMask.indexOf(".");
//                parentIndex =  parentIpMask.indexOf(".", parentIndex + 1);
//                parentIndex =  parentIpMask.indexOf(".", parentIndex + 1);
//            } else if (parentMask == 16) {
//                parentIndex =  parentIpMask.indexOf(".");
//                parentIndex =  parentIpMask.indexOf(".", parentIndex + 1);
//            } else if (parentMask == 8) {
//                parentIndex =  parentIpMask.indexOf(".");
//            }
//            parentIpSeggment = parentIpMask.substring(0, parentIndex);
//            parentSegment.put(parentIpSeggment, parentIpMask);
//        }
//
//        // 判断是否属于第一级
//        for (Map.Entry<String, Integer> entry : otherMap.entrySet()) {
//            Integer mask = entry.getValue();
//            String ip = entry.getKey();
//            String ip_segment = null;
//            int index = 0;
//            if (mask > 24) {
//                index =  ip.indexOf(".");
//                index =  ip.indexOf(".", index + 1);
//                index =  ip.indexOf(".", index + 1);
//            } else if (24 >= mask && mask > 16) {
//                index =  ip.indexOf(".");
//                index =  ip.indexOf(".", index + 1);
//            } else if (16 >= mask && mask > 8) {
//                index =  ip.indexOf(".");
//            }
//            ip_segment = ip.substring(0, index);
//            if(parentSegment.get(ip_segment) != null){
//                List<Object> list = parentMap.get(parentSegment.get(ip_segment));
//                list.add(ip + "/" + mask);
//            }else{
//                Integer parentMask = null;
//                if (mask > 24) {
//                    parentMask = 24;
//                } else if (24 >= mask && mask > 16) {
//                    parentMask = 16;
//                } else if (16 >= mask && mask > 8) {
//                    parentMask = 8;
//                }
//                String parentIp = this.getParentIp(ip, parentMask);
//                parentIp = parentIp + "/" + parentMask;
//                List<Object> list = new ArrayList<>();
//                list.add(ip + "/" + mask);
//                parentMap.put(parentIp, list);
//                parentSegment.put(ip_segment, parentIp);
//            }
//        }
//        // 遍历二级ip，生成上级Ip
//        if(parentMap.size() > 1){
//            Map<String, List<Object>> parent = this.getShortMask(parentMap);
//            if(parent != null && parent.size() > 0){
//                return parent;
//            }
//        }else{}
//        return parentMap;
//    }

    /**
     *
     * @param ip
     * @param bitmask
     * @return
     */
    public String getParentIp(String ip, Integer bitmask){
        String segment = "";
        if (24 == bitmask) {
            String mask = Ipv4Util.bitMaskConvertMask(bitmask);
            segment = Ipv4Util.getNetwork(ip, mask);
        } else if (16  == bitmask) {
            String mask = Ipv4Util.bitMaskConvertMask(bitmask);
            segment = Ipv4Util.getNetwork(ip, mask);
        }else if (8  == bitmask) {
            String mask = Ipv4Util.bitMaskConvertMask(bitmask);
            segment = Ipv4Util.getNetwork(ip, mask);
        }
        return segment;
    }

    // 获取网段
//    public String getParentIp(String ip, Integer mask){
//        int index = 0;
//        String segment = "";
//        if (24 == mask) {
//            index =  ip.indexOf(".");
//            index =  ip.indexOf(".", index + 1);
//            index =  ip.indexOf(".", index + 1);
//            segment = ".0";
//        } else if (16  == mask) {
//            index =  ip.indexOf(".");
//            index =  ip.indexOf(".", index + 1);
//            segment = ".0.0";
//        }else if (8  == mask) {
//            index =  ip.indexOf(".");
//            segment = ".0.0.0";
//        }
//        String parentIp = ip.substring(0, index);
//        return parentIp + segment;
//    }

    public Map<String, List<Object>> getShortMask(Map<String, List<Object>> parentMap){
//        String parentIp = null;
        Integer shorMask = 0;
        for (Map.Entry<String, List<Object>> entry : parentMap.entrySet()){
            String ip = entry.getKey();
            int index = ip.indexOf("/");
            int mask = Integer.parseInt(ip.substring(index + 1));
            if(mask > shorMask || shorMask == 0){
                shorMask = mask;
            }
        }
        // 遍历parentMap 获取掩码位等于parentmask网段集合

        Map<String, List<Object>> map = new HashMap<>();

        for (Map.Entry<String, List<Object>> entry : parentMap.entrySet()){
            String ipMask = entry.getKey();
            int index = ipMask.indexOf("/");
            int mask = Integer.parseInt(ipMask.substring(index + 1));
            // 判断当前mask是否等于最短mask
            if(mask != shorMask){
                map.put(ipMask, parentMap.get(ipMask));
            }
        }
        for (Map.Entry<String, List<Object>> entry : parentMap.entrySet()){
            String ipMask = entry.getKey();
            int index = ipMask.indexOf("/");
            int mask = Integer.parseInt(ipMask.substring(index + 1));
            String ip = ipMask.substring(0, index);
            Integer parentMask = null;
            // 判断当前mask是否等于最短mask
            if(mask == shorMask){
                // 同为最低等级mask/创建上级
                if (mask > 24) {
                    parentMask = 24;
                } else if (24 >= mask && mask > 16) {
                    parentMask = 16;
                } else if (16 >= mask && mask > 8) {
                    parentMask = 8;
                }
                // 生成上级网段
                String parentIp = this.getParentIp(ip, parentMask);
                parentIp = parentIp + "/" + parentMask;
                // 比较是否已经存在
                if(map.get(parentIp) != null){

                    List<Object> list = map.get(parentIp);

                    List<Object> childs = parentMap.get(ipMask);

                    Map child = new HashMap();
                    child.put(ipMask, childs);

                    list.add(child);

                    map.put(parentIp, list);

                }else{
                    List<Object> list =  new ArrayList<>();

                    List<Object> childs = parentMap.get(ipMask);

                    Map child = new HashMap();
                    child.put(ipMask, childs);

                    list.add(child);

                    map.put(parentIp, list);
                }
            }
        }
        return map;
    }

    @ApiOperation("根据网段Ip查询直接从属子网")
    @GetMapping(value = {"", "/{id}"})
    public Object getSubnet(@PathVariable(value = "id", required = false) Long id) {
        if (id == null) {
            // 获取所有子网一级
            List<Subnet> parentList = this.subnetService.selectSubnetByParentId(null);
            if (parentList.size() > 0) {
                for (Subnet subnet : parentList) {
                    this.genericSubnet(subnet);
                }
                return ResponseUtil.ok(parentList);
            }
        } else {
            // 校验子网是否存在
            Subnet subnet = this.subnetService.selectObjById(id);
            if (subnet != null) {
                // 当前网段
                Map map = new HashMap();
                map.put("subnet", subnet);
                // 获取从子网列表
                List<Subnet> subnetList = this.subnetService.selectSubnetByParentId(id);
                //
                map.put("subnets", subnetList);
                // 查询IP addresses in subnets
                if (subnetList.size() <= 0 && subnet.getMask() >= 24) {
                    // 获取地址列表
                    // 获取最大Ip地址和最小Ip地址
                    String mask = Ipv4Util.bitMaskConvertMask(subnet.getMask());
                    Map networkMap = Ipv4Util.getNetworkIp(subnet.getIp(), mask);
                    String[] ips = Ipv4Util.getSubnetList(networkMap.get("network").toString(),
                            subnet.getMask());
                    if (ips.length > 0) {
                        Map addresses = new LinkedHashMap();
                        for (String ip : ips) {

                            Address address = this.addressService.selectObjByIp(Ipv4Util.ipConvertDec(ip));

                            if (address != null) {
                                Ipv4Detail ipv4Detail = this.ipV4DetailService.selectObjByIp(Ipv4Util.ipConvertDec(ip));
                                if (ipv4Detail != null) {
                                    int time = ipv4Detail.getTime();
                                    // 每分钟采一次
                                    int hourAll = time / 60;// 一共多少小时
                                    int day = hourAll / 24;
                                    int hour = hourAll % 24;
                                    ipv4Detail.setDuration(day + "天" + hour + "小时");
                                    address.setIpDetail(ipv4Detail);
                                }
                                // 写入Ip地址的设备信息
                                Map deviceInfo = this.rsmsDeviceUtils.getDeviceInfo(address.getIp());
                                address.setDeviceInfo(deviceInfo);
                            }
                            addresses.put(ip, address);
                        }
                        map.put("addresses", addresses);
                    }
                } else if (subnetList.size() <= 0 && subnet.getMask() < 24 && subnet.getMask() >= 16) {
                    // 获取网段数量
                    String mask = Ipv4Util.bitMaskConvertMask(subnet.getMask());
                    Map networkMap = Ipv4Util.getNetworkIp(subnet.getIp(), mask);
                    String[] ips = Ipv4Util.getSubnetList(networkMap.get("network").toString(),
                            subnet.getMask());
                    int sum = ips.length / 255;
                    List list = new ArrayList();
                    if (sum > 0) {
                        for (int i = 0; i < sum; i++) {
                            String ip = subnet.getIp();
                            String[] seg = ip.split("\\.");
                            StringBuffer buffer = new StringBuffer();
                            buffer.append(seg[0]).append(".").append(seg[1]).append(".").append(i).append(".").append(seg[3]).append("-").append("255");
                            list.add(buffer);
                        }
                    }
                    map.put("segmentation", list);
                } else {
                    // 查询子网ip地址列表
                    Map params = new HashMap();
                    params.put("subnetId", subnet.getId());
                    List<Address> address = this.addressService.selectObjByMap(params);
                    map.put("mastSubnetAddress", address);
                }
                return ResponseUtil.ok(map);
            }
            return ResponseUtil.badArgument("网段不存在");
        }
        return ResponseUtil.ok();
    }

    @ApiOperation("Ip使用率")
    @GetMapping("/picture")
    public Object picture(@RequestParam(value = "subnetId", required = false) Long subnetId) {
        // 根据子网查询address
        Set<Long> subnetIds = new HashSet<>();
        if (subnetId != null) {
            Set<Long> ids = this.genericSubnet(subnetId);
            subnetIds.addAll(ids);
        }
        Map params = new HashMap();
        if (subnetIds.size() > 0) {
            params.clear();
            params.put("subnetIds", subnetIds);
            List<Address> addresses = this.addressService.selectObjByMap(params);
            if (addresses.size() > 0) {
                List<String> ips = new ArrayList<>();
                addresses.forEach((item) -> {
                    ips.add(Ipv4Util.ipConvertDec(item.getIp()));
                });
                params.clear();
                params.put("ips", ips);
                params.put("usage", 0);
                List<Ipv4Detail> unuseds = this.ipV4DetailService.selectObjByMap(params);

                params.clear();
                params.put("ips", ips);
                params.put("start", 1);
                params.put("end", 2);
                List<Ipv4Detail> seldom = this.ipV4DetailService.selectObjByMap(params);

                params.clear();
                params.put("ips", ips);
                params.put("start", 3);
                params.put("end", 9);
                List<Ipv4Detail> unmeant = this.ipV4DetailService.selectObjByMap(params);

                params.clear();
                params.put("ips", ips);
                params.put("endUsage", 10);
                List<Ipv4Detail> regular = this.ipV4DetailService.selectObjByMap(params);

                List<Integer> list = this.genericSubnetIps(subnetId);

                int sum = list.stream().mapToInt((s) -> s).sum();

                int existingSum = unuseds.size() + seldom.size() + unmeant.size() + regular.size();

                int inexistenceSum = sum - existingSum;

                float unusedScale = (float) (unuseds.size() + inexistenceSum) / sum;

                Map map = new HashMap();
                map.put("unused", Math.round(unusedScale * 100));

                float seldomScale = (float) seldom.size() / sum;

                map.put("seldom", Math.round(seldomScale * 100));

                float unmeantScale = (float) unmeant.size() / sum;

                map.put("unmeant", Math.round(unmeantScale * 100));

                float regularScale = (float) regular.size() / sum;

                map.put("regular", Math.round(regularScale * 100));

                return ResponseUtil.ok(map);
            }
        }

        return ResponseUtil.ok();
    }

    /**
     * 获取所有从属子网Id
     *
     * @param id
     * @return
     */
    public Set<Long> genericSubnet(Long id) {
        Set<Long> ids = new HashSet();
        Subnet subnet = this.subnetService.selectObjById(id);
        if (subnet != null) {
            ids.add(id);
            List<Subnet> childs = this.subnetService.selectSubnetByParentId(subnet.getId());
            if (childs.size() > 0) {
                for (Subnet obj : childs) {
                    Set<Long> cids = genericSubnet(obj.getId());
                    ids.addAll(cids);
                    ids.add(obj.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 获取从属子网ips
     *
     * @param id
     * @return
     */
    public List<Integer> genericSubnetIps(Long id) {
        List<Integer> list = new ArrayList();
        Subnet subnet = this.subnetService.selectObjById(id);
        if (subnet != null) {
            // 从属子网
            List<Subnet> subnets = this.subnetService.selectSubnetByParentId(subnet.getId());
            if (subnets.size() > 0) {
                for (Subnet obj : subnets) {
                    List<Integer> clengs = genericSubnetIps(obj.getId());
                    list.addAll(clengs);
                    List<Subnet> csubnets = this.subnetService.selectSubnetByParentId(subnet.getId());
                    // 查询IP addresses in subnets
                    if (csubnets.size() <= 0 && obj.getMask() >= 16) {
                        // 获取地址列表
                        // 获取最大Ip地址和最小Ip地址
                        String mask = Ipv4Util.bitMaskConvertMask(obj.getMask());
                        Map networkMap = Ipv4Util.getNetworkIpDec(obj.getIp(), mask);
                        String[] ips = Ipv4Util.getSubnetList(networkMap.get("network").toString(),
                                obj.getMask());
                        if (ips.length > 0) {
                            list.add(ips.length);
                        }
                    }
                }
            }
            // 查询IP addresses in subnets
            if (subnets.size() <= 0 && subnet.getMask() >= 16) {
                // 获取地址列表
                // 获取最大Ip地址和最小Ip地址
                String mask = Ipv4Util.bitMaskConvertMask(subnet.getMask());
                Map networkMap = Ipv4Util.getNetworkIp(subnet.getIp(), mask);
                String[] ips = Ipv4Util.getSubnetList(networkMap.get("network").toString(),
                        subnet.getMask());
                if (ips.length > 0) {
                    list.add(ips.length);
                }
            }
        }
        return list;
    }

    @PutMapping
    public Result update(@RequestBody Subnet instance){
        return this.subnetService.update(instance);
    }
}
