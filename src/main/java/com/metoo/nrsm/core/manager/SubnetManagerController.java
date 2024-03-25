package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IPortService;
import com.metoo.nrsm.core.service.ISubnetService;
import com.metoo.nrsm.core.utils.ip.IpV4Util;
import com.metoo.nrsm.entity.Address;
import com.metoo.nrsm.entity.Port;
import com.metoo.nrsm.entity.Subnet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Api("子网管理")
@RequestMapping("/admin/subnet")
@RestController
public class SubnetManagerController {

    @Autowired
    private ISubnetService subnetService;
    @Autowired
    private IPortService portService;

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

    @ApiOperation("根据网段id查询直接从属子网")
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
                    String mask = IpV4Util.bitMaskConvertMask(subnet.getMask());
                    Map networkMap = IpV4Util.getNetworkIp(subnet.getIp(), mask);
                    String[] ips = IpV4Util.getSubnetList(networkMap.get("network").toString(),
                            subnet.getMask());
//                    if (ips.length > 0) {
//                        Map addresses = new LinkedHashMap();
//                        for (String ip : ips) {
//                            Address address = this.addressService.selectObjByIp(IpUtil.ipConvertDec(ip));
//                            if (address != null) {
//                                IpDetail ipDetail = this.ipDetailService.selectObjByIp(IpUtil.ipConvertDec(ip));
//                                if (ipDetail != null) {
//                                    int time = ipDetail.getTime();
//                                    // 每分钟采一次
//                                    int hourAll = time / 60;// 一共多少小时
//                                    int day = hourAll / 24;
//                                    int hour = hourAll % 24;
//                                    ipDetail.setDuration(day + "天" + hour + "小时");
//                                    address.setIpDetail(ipDetail);
//                                }
//                                // 写入Ip地址的设备信息
//                                Map deviceInfo = this.rsmsDeviceUtils.getDeviceInfo(address.getIp());
//                                address.setDeviceInfo(deviceInfo);
//                            }
//                            addresses.put(ip, address);
//                        }
//                        map.put("addresses", addresses);
//                    }
                } else if (subnetList.size() <= 0 && subnet.getMask() < 24 && subnet.getMask() >= 16) {
                    // 获取网段数量
                    String mask = IpV4Util.bitMaskConvertMask(subnet.getMask());
                    Map networkMap = IpV4Util.getNetworkIp(subnet.getIp(), mask);
                    String[] ips = IpV4Util.getSubnetList(networkMap.get("network").toString(),
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
//                    Map params = new HashMap();
//                    params.put("subnetId", subnet.getId());
//                    List<Address> address = this.addressService.selectObjByMap(params);
//                    map.put("mastSubnetAddress", address);
                }
                return ResponseUtil.ok(map);
            }
            return ResponseUtil.badArgument("网段不存在");
        }
        return ResponseUtil.ok();
    }

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


    @RequestMapping(value = {"/comb"})
    public Object comb() {
        Map params = new HashMap();
        params.put("ipIsNotNull", "is not null");
        params.put("NotIp", IpV4Util.ipConvertDec("127.0.0.1"));
        List<Port> ports = this.portService.selectObjByMap(params);
        Map<String, List<Object>> map = this.ipAddressCombingByDB(ports);
        for (String key : map.keySet()) {
            int index = key.indexOf("/");
            String firstIp = key.substring(0, index);
            int firstMask = Integer.parseInt(key.substring(index + 1));
            // 插入本地数据库 and 同步ipam
            // 插如一级ip网段
            Long firstSubnetId = null;
            Subnet firstSubnet = this.subnetService.selectObjByIpAndMask(firstIp, firstMask);
            if (firstSubnet != null) {
                firstSubnetId = firstSubnet.getId();
            }
            if (firstSubnet == null) {
                Subnet subnet = new Subnet();
                subnet.setIp(IpV4Util.ipConvertDec(firstIp));
                subnet.setMask(firstMask);
                this.subnetService.save(subnet);
                firstSubnetId = subnet.getId();
            }
            // 获取二级网段
            JSONArray array = JSONArray.parseArray(JSON.toJSONString(map.get(key)));
            for (Object obj : array) {
                if (obj instanceof String) {
                    String second = ((String) obj).trim();
                    int sequence = second.indexOf("/");
                    String ip = second.substring(0, sequence);
                    int secondMask = Integer.parseInt(second.substring(sequence + 1));
                    Subnet secondSubnet = this.subnetService.selectObjByIpAndMask(ip, secondMask);
                    if (secondSubnet == null) {
                        Subnet subnet = new Subnet();
                        subnet.setIp(IpV4Util.ipConvertDec(ip));
                        subnet.setMask(secondMask);
                        subnet.setParentIp(IpV4Util.ipConvertDec(firstIp));
                        subnet.setParentId(firstSubnetId);
                        this.subnetService.save(subnet);
                    }
                }
                if (obj instanceof JSONObject) {
                    JSONObject object = (JSONObject) obj;
                    for (String okey : object.keySet()) {
                        String second = okey.trim();
                        int sequence = second.indexOf("/");
                        String secondIp = second.substring(0, sequence);
                        int secondMask = Integer.parseInt(second.substring(sequence + 1));
                        Long secondSubnetId = null;
                        Subnet secondSubnet = this.subnetService.selectObjByIpAndMask(secondIp, secondMask);
                        if (secondSubnet != null) {
                            secondSubnetId = secondSubnet.getId();
                        }
                        if (secondSubnet == null) {
                            Subnet subnet = new Subnet();
                            subnet.setIp(IpV4Util.ipConvertDec(secondIp));
                            subnet.setMask(secondMask);
                            subnet.setParentIp(IpV4Util.ipConvertDec(firstIp));
                            subnet.setParentId(firstSubnetId);
                            this.subnetService.save(subnet);
                            secondSubnetId = subnet.getId();
                        }
                        JSONArray thirdArray = JSONArray.parseArray(object.get(okey).toString());
                        for (Object thirdKey : thirdArray) {
                            if (obj instanceof JSONObject) {
                                String third = ((String) thirdKey).trim();
                                int thirdSequence = third.indexOf("/");
                                String thirdIp = third.substring(0, thirdSequence);
                                int thirdMask = Integer.parseInt(third.substring(thirdSequence + 1));
                                Subnet thirdSubnet = this.subnetService.selectObjByIpAndMask(thirdIp, thirdMask);
                                if (thirdSubnet == null) {
                                    Subnet subnet = new Subnet();
                                    subnet.setIp(IpV4Util.ipConvertDec(thirdIp));
                                    subnet.setMask(thirdMask);
                                    subnet.setParentIp(IpV4Util.ipConvertDec(secondIp));
                                    subnet.setParentId(secondSubnetId);
                                    this.subnetService.save(subnet);
                                }
                            }
                        }
                    }
                }
            }
        }

        return ResponseUtil.ok();
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
            Integer maskBit = IpV4Util.getMaskBitByMask(mask);
            Map networkMap = IpV4Util.getNetworkIp(ip, mask);
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
            String parentIp = this.getParentIp(ip, parentMask);
            parentIp = parentIp + "/" + parentMask;
            if (!parentMap.keySet().contains(parentIp) && parentMap.get(parentIp) == null) {
                List<Object> list = new ArrayList<>();
                list.add(ip + "/" + maskBit);
                parentMap.put(parentIp, list);
            } else {
                List<Object> list = parentMap.get(parentIp);
                if(list != null){
                    list.add(ip + "/" + maskBit);
                }
            }
        }
//        遍历
        Map parentSegment = new HashMap();
        for (Map.Entry entry1 : parentMap.entrySet()) {
            String parentIpMask = (String) entry1.getKey();
            Integer parentMask = null;
            int sequence = parentIpMask.indexOf("/");
            parentMask = Integer.parseInt(parentIpMask.substring(sequence + 1));
            int parentIndex = 0;
            String parentIpSeggment = null;
            if (parentMask == 24) {
                parentIndex =  parentIpMask.indexOf(".");
                parentIndex =  parentIpMask.indexOf(".", parentIndex + 1);
                parentIndex =  parentIpMask.indexOf(".", parentIndex + 1);
            } else if (parentMask == 16) {
                parentIndex =  parentIpMask.indexOf(".");
                parentIndex =  parentIpMask.indexOf(".", parentIndex + 1);
            } else if (parentMask == 8) {
                parentIndex =  parentIpMask.indexOf(".");
            }
            parentIpSeggment = parentIpMask.substring(0, parentIndex);
            parentSegment.put(parentIpSeggment, parentIpMask);
        }

        // 判断是否属于第一级
        for (Map.Entry<String, Integer> entry : otherMap.entrySet()) {
            Integer mask = entry.getValue();
            String ip = entry.getKey();
            String ip_segment = null;
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
            ip_segment = ip.substring(0, index);
            if(parentSegment.get(ip_segment) != null){
                List<Object> list = parentMap.get(parentSegment.get(ip_segment));
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
                parentIp = parentIp + "/" + parentMask;
                List<Object> list = new ArrayList<>();
                list.add(ip + "/" + mask);
                parentMap.put(parentIp, list);
                parentSegment.put(ip_segment, parentIp);
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

    /**
     *
     * @param ip
     * @param mask
     * @return
     */
    public String getParentIp(String ip, Integer mask){
        int index = 0;
        String segment = "";
        if (24 == mask) {
            index =  ip.indexOf(".");
            index =  ip.indexOf(".", index + 1);
            index =  ip.indexOf(".", index + 1);
            segment = ".0";
        } else if (16  == mask) {
            index =  ip.indexOf(".");
            index =  ip.indexOf(".", index + 1);
            segment = ".0.0";
        }else if (8  == mask) {
            index =  ip.indexOf(".");
            segment = ".0.0.0";
        }
        String parentIp = ip.substring(0, index);
        return parentIp + segment;
    }

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
}
