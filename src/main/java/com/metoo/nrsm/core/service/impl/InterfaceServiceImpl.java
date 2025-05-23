package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.mapper.InterfaceMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.system.conf.network.strategy.NetplanConfigManager;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.Vlans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InterfaceServiceImpl implements IInterfaceService {

    @Autowired
    private InterfaceMapper interfaceMapper;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Override
    public Interface selectObjById(Long id) {
        return this.interfaceMapper.selectObjById(id);
    }

    @Override
    public List<Interface> selectObjByParentId(Long parentId) {
        return this.interfaceMapper.selectObjByParentId(parentId);
    }

    @Override
    public Interface selectObjByName(String name) {
        return this.interfaceMapper.selectObjByName(name);
    }

    @Override
    public List<Interface> selectParentInterfaces(List<Long> parentIds){
        return this.interfaceMapper.selectParentInterfaces(parentIds);
    }

    @Override
    public Page<Interface> selectObjConditionQuery(InterfaceDTO dto) {
        if (dto == null) {
            dto = new InterfaceDTO();
        }

        Page<Interface> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<Interface> parentInterfaces = this.interfaceMapper.selectObjConditionQuery(dto);

        // 获取所有父接口的id，用来查询子接口
        List<Long> parentIds = parentInterfaces.stream()
                .map(Interface::getId)
                .collect(Collectors.toList());

        if (!parentIds.isEmpty()) {
            // 查询所有子接口
            List<Interface> subInterfaces = interfaceMapper.selectParentInterfaces(parentIds);

            // 将子接口映射到父接口上
            for (Interface parent : parentInterfaces) {
                List<Interface> childList = subInterfaces.stream()
                        .filter(sub -> sub.getParentId().equals(parent.getId()))
                        .collect(Collectors.toList());
                parent.setVlans(childList);
            }
        }

        return page;
    }

    @Override
    public List<Interface> selectObjByMap(Map params) {
        return this.interfaceMapper.selectObjByMap(params);
    }

//    @Override
//    public int save(Interface instance) {
//        if(instance.getId() == null || instance.getId().equals("")){
//            try {
//                if(modify_ip(instance.getName(), instance.getIpv4Address(),
//                        instance.getIpv6Address(), instance.getGateway4(), instance.getGateway6())){
//                    instance.setAddTime(new Date());
//                    int i = this.interfaceMapper.save(instance);
//                    return i;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return 0;
//            }
//        }else{
//            try {
//                if(modify_ip(instance.getName(), instance.getIpv4Address(),
//                        instance.getIpv6Address(), instance.getGateway4(), instance.getGateway6())){
//                    instance.setAddTime(new Date());
//                    int i = this.interfaceMapper.update(instance);
//                    return i;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return 0;
//            }
//        }
//        return 0;
//    }

    @Override
    public int save(Interface instance) {

        // 更新配置文件
        updateConfig(instance);

        int i = 0;
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());
            try {
                i = this.interfaceMapper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                i = this.interfaceMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return i;
    }


    public void updateConfig(Interface instance){
        Interface mastInterface = null;
        if(instance.getParentId() != null){
            // 清空主接口配置数据
            mastInterface = this.interfaceMapper.selectObjById(instance.getParentId());
            if(mastInterface != null && (mastInterface.getIpv4Address() != null || StringUtil.isNotEmpty(mastInterface.getIpv4Address()))){
                mastInterface.setGateway4(null);
                mastInterface.setIpv4Address(null);
                mastInterface.setIpv4netmask(null);
                mastInterface.setGateway6(null);
                mastInterface.setIpv6Address(null);
                mastInterface.setIpv6netmask(null);
                this.interfaceMapper.update(mastInterface);
                try {
                    // 清空主接口数据，并更新配置文件
                    NetplanConfigManager.updateInterfaceConfig(mastInterface);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            if(mastInterface != null){
                instance.setParentName(mastInterface.getName());
            }
            // 清空主接口数据，并更新配置文件
            NetplanConfigManager.updateInterfaceConfig(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<Interface> select() {
        List<Interface> list = new ArrayList<>();
        String result = SNMPv2Request.getNetworkInterfaces();
        if(!"".equals(result)){
            LinkedHashMap<String, Object> map = JSONObject.parseObject(result, LinkedHashMap.class);
            for (String key : map.keySet()) {
                Interface inteface = JSONObject.parseObject(JSONObject.toJSONString(map.get(key)), Interface.class);
                inteface.setName(key);
                list.add(inteface);
            }
            return list;
        }
        return null;
    }

    @Override
    public int update(Interface instance) {
        try {
            // 如果是vlan接口，拼接vlan接口名
            if(instance.getParentId() != null && instance.getVlanNum() != null){
                Interface mastInterface = this.interfaceMapper.selectObjById(instance.getParentId());
                instance.setParentName(mastInterface.getName());
            }
            NetplanConfigManager.updateInterfaceConfig(instance);

            int i = this.interfaceMapper.update(instance);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            try {
                Interface instance = this.interfaceMapper.selectObjById(id);
                // 如果是vlan接口，拼接vlan接口名
                if(instance.getParentId() != null && instance.getVlanNum() != null){
                    Interface mastInterface = this.interfaceMapper.selectObjById(id);
                    String name = mastInterface.getName() + "." + instance.getVlanNum();
                    NetplanConfigManager.removeVlanInterface(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int i = this.interfaceMapper.delete(id);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean modify_ip(Interface instance){
//        String path = Global.PYPATH + "modifyip.py";
//        String[] params = {instance.getName(), instance.getIpv4Address(),
//                instance.getIpv6Address(), instance.getGateway4(), instance.getGateway6()};
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPv2Request.modifyIp(instance.getName(), instance.getIpv4Address(), instance.getIpv6Address(), instance.getGateway4(), instance.getGateway6());
        if(result.equals("0")){
            return true;
        }
        return false;
    }


    // TODO Vlan改用Interface
    @Override
    public boolean modify_vlans(String name, Interface instance){
        String result = SNMPv2Request.modifyVlans(name, String.valueOf(instance.getVlanNum()), instance.getIpv4Address(),instance.getIpv6Address(),instance.getGateway4(),instance.getGateway6());
        if(result.equals("0")){
            return true;
        }
        return false;
    }

}
