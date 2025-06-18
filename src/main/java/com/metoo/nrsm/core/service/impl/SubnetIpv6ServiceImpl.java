package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.PortIpv6Mapper;
import com.metoo.nrsm.core.mapper.SubnetIpv6Mapper;
import com.metoo.nrsm.core.service.ISubnetIpv6Service;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.SubnetIpv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-24 14:42
 */
@Service
//@Transactional
public class SubnetIpv6ServiceImpl implements ISubnetIpv6Service {

    @Autowired
    private SubnetIpv6Mapper subnetIpv6Mapper;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PortIpv6Mapper portIpv6Mapper;

    @Override
    public SubnetIpv6 selectObjById(Long id) {
        return this.subnetIpv6Mapper.selectObjById(id);
    }

    @Override
    public List<SubnetIpv6> selectSubnetByParentId(Long id) {
        return this.subnetIpv6Mapper.selectSubnetByParentId(id);
    }

    @Override
    public boolean save(SubnetIpv6 instance) {
        try {
            instance.setAddTime(new Date());
            this.subnetIpv6Mapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Result update(SubnetIpv6 instance) {
        try {
            if(this.selectObjById(instance.getId()) != null) {
                int i = this.subnetIpv6Mapper.update(instance);
                if(i >= 0){
                    return ResponseUtil.ok();
                }
            }
            return ResponseUtil.dataNotFound();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error();
        }
    }

    @Override
    public Result getSubnet() {
        this.subnetIpv6Mapper.truncateTable();
//        String path = Global.PYPATH + "subnetipv6.py";
//        String result = pythonExecUtils.exec(path);
        String result = analyzeSubnets();
        if(!"".equals(result)){
            JSONObject obj = JSONObject.parseObject(result);
            if(obj != null){
                generic(obj, null);
                return ResponseUtil.ok();
            }
        }
        return ResponseUtil.ok("网段梳理数据为空");
    }

    // IPv6 工具类
    public static class IPv6Utils {
        public static String calculateNetwork(String address, int prefix) {
            try {
                Inet6Address ip = (Inet6Address) InetAddress.getByName(address.split("/")[0]);
                byte[] bytes = ip.getAddress();
                applyPrefixMask(bytes, prefix);
                return formatIPv6(bytes) + "/" + prefix;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid IPv6: " + address);
            }
        }

        private static void applyPrefixMask(byte[] bytes, int prefix) {
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;

            // 处理完整字节
            for (int i = fullBytes + 1; i < 16; i++) {
                bytes[i] = 0;
            }

            // 处理剩余位
            if (remainingBits > 0 && fullBytes < 15) {
                int mask = 0xFF << (8 - remainingBits);
                bytes[fullBytes] = (byte) (bytes[fullBytes] & mask);
            }
        }

        private static String formatIPv6(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                if (i > 0) sb.append(":");
                sb.append(String.format("%02x%02x", bytes[i], bytes[i + 1]));
            }
            return sb.toString()
                    .replaceAll("(:0){2,}", "::")
                    .replaceAll("^0::", "::");
        }
    }

    public String analyzeSubnets() {
        List<String> ipv6List = portIpv6Mapper.selectIpv6Cidrs();
        Map<String, List<String>> subnet64 = new LinkedHashMap<>();
        Map<String, List<String>> subnet56 = new LinkedHashMap<>();

        // 生成去重网络列表
        Set<String> networks = new LinkedHashSet<>();
        for (String ip : ipv6List) {
            String[] parts = ip.split("/");
            networks.add(IPv6Utils.calculateNetwork(parts[0], Integer.parseInt(parts[1])));
        }

        // 构建64位子网结构
        for (String network : networks) {
            String[] parts = network.split("/");
            int prefix = Integer.parseInt(parts[1]);
            String base64 = IPv6Utils.calculateNetwork(parts[0], 64);

            if (prefix > 64) {
                subnet64.computeIfAbsent(base64, k -> new ArrayList<>()).add(network);
            } else {
                subnet64.putIfAbsent(base64, new ArrayList<>());
            }
        }

        // 构建56位父网络
        for (String network64 : subnet64.keySet()) {
            String[] parts = network64.split("/");
            String base56 = IPv6Utils.calculateNetwork(parts[0], 56);
            subnet56.computeIfAbsent(base56, k -> new ArrayList<>()).add(network64);
        }

        // 生成嵌套结构
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry56 : subnet56.entrySet()) {
            Map<String, List<String>> children = new LinkedHashMap<>();
            for (String network64 : entry56.getValue()) {
                children.put(network64, subnet64.get(network64));
            }
            result.put(entry56.getKey(), children);
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("JSON生成失败", e);
        }
    }

    @Override
    public int truncateTable() {
        return this.subnetIpv6Mapper.truncateTable();
    }

    public void generic(JSONObject obj, Long parentId) {
        if (obj != null) {
            for (String key : obj.keySet()) {
                Object value = obj.get(key);
                if (value != null) {
                    // 保存当前子网信息
                    SubnetIpv6 subnetIpv6 = new SubnetIpv6();
                    subnetIpv6.setIp(key.split("/")[0]);
                    subnetIpv6.setMask(Integer.parseInt(key.split("/")[1]));
                    subnetIpv6.setParentId(parentId);
                    this.subnetIpv6Mapper.save(subnetIpv6);

                    // 处理子节点
                    if (value instanceof JSONObject) {
                        // 如果值是JSON对象，递归处理
                        JSONObject childObj = (JSONObject) value;
                        generic(childObj, subnetIpv6.getId());
                    } else if (value instanceof JSONArray) {
                        // 如果值是JSON数组，遍历元素
                        JSONArray childs = (JSONArray) value;
                        for (Object ele : childs) {
                            if (ele instanceof String) {
                                // 处理字符串类型的子网（如 "C/65"）
                                SubnetIpv6 child = new SubnetIpv6();
                                String[] parts = ((String) ele).split("/");
                                child.setIp(parts[0]);
                                child.setMask(Integer.parseInt(parts[1]));
                                child.setParentId(subnetIpv6.getId());
                                this.subnetIpv6Mapper.save(child);
                            } else if (ele instanceof JSONObject) {
                                // 处理嵌套对象（如果有）
                                generic((JSONObject) ele, subnetIpv6.getId());
                            }
                        }
                    }
                }
            }
        }
    }

}
