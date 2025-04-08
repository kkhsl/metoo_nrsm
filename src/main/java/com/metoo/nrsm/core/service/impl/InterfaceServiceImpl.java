package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.InterfaceDTO;
import com.metoo.nrsm.core.mapper.InterfaceMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPRequest;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Interface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public Page<Interface> selectObjConditionQuery(InterfaceDTO dto) {
        if (dto == null) {
            dto = new InterfaceDTO();
        }

        Page<Interface> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.interfaceMapper.selectObjConditionQuery(dto);
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
//                if(modify_ip(instance.getName(), instance.getIpv4address(),
//                        instance.getIpv6address(), instance.getGateway4(), instance.getGateway6())){
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
//                if(modify_ip(instance.getName(), instance.getIpv4address(),
//                        instance.getIpv6address(), instance.getGateway4(), instance.getGateway6())){
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
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());

            try {
                int i = this.interfaceMapper.save(instance);
                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {

                int i = this.interfaceMapper.update(instance);
                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(Interface instance) {
        try {

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
//        String[] params = {instance.getName(), instance.getIpv4address(),
//                instance.getIpv6address(), instance.getGateway4(), instance.getGateway6()};
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPRequest.modifyIp(instance.getName(), instance.getIpv4address(), instance.getIpv6address(), instance.getGateway4(), instance.getGateway6());
        if(result.equals("0")){
            return true;
        }
        return false;
    }

}
