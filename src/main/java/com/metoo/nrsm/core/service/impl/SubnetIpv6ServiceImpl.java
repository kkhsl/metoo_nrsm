package com.metoo.nrsm.core.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.SubnetIpv6Mapper;
import com.metoo.nrsm.core.service.ISubnetIpv6Service;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.py.ssh.SshExec;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Subnet;
import com.metoo.nrsm.entity.SubnetIpv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
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
    public void getSubnet() {
        this.subnetIpv6Mapper.truncateTable();
        String path = Global.PYPATH + "subnetipv6.py";
        String result = PythonExecUtils.exec(path);
        if(!"".equals(result)){
            JSONObject obj = JSONObject.parseObject(result);
            if(obj != null){
                generic(obj, null);
            }
        }
    }

    @Override
    public int truncateTable() {
        return this.subnetIpv6Mapper.truncateTable();
    }

    public void generic(JSONObject obj, Long parentId){
        if(obj != null){
            for (String key : obj.keySet()) {
                if(obj.get(key) != null){
                    SubnetIpv6 subnetIpv6 = new SubnetIpv6();
                    subnetIpv6.setIp(key.split("/")[0]);
                    subnetIpv6.setMask(Integer.parseInt(key.split("/")[1]));
                    subnetIpv6.setParentId(parentId);
                    this.subnetIpv6Mapper.save(subnetIpv6);
                    JSONArray childs = JSONObject.parseArray(obj.getString(key));
                    if(childs.size() > 0){
                        for (Object ele : childs) {
                            if(ele instanceof String){
                                SubnetIpv6 child = new SubnetIpv6();
                                child.setIp(String.valueOf(ele).split("/")[0]);
                                child.setMask(Integer.parseInt(String.valueOf(ele).split("/")[1]));
                                child.setParentId(subnetIpv6.getId());
                                this.subnetIpv6Mapper.save(child);
                            } if(ele instanceof JSONObject){
                                JSONObject child = JSONObject.parseObject(JSONObject.toJSONString(ele));
                                generic(child, subnetIpv6.getId());
                            } else {}
                        }
                    }
                }
            }
        }
    }

}
