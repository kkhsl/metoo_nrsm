package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.DnsMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.IDNSService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Dns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DNSServiceImpl implements IDNSService {

    @Autowired
    private DnsMapper dnsMapper;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Override
    public List<Dns> selectObjByMap(Map params) {
        return this.dnsMapper.selectObjByMap(params);
    }

    @Override
    public List<Dns> selectObjByPrimaryDomain(String primaryDomain) {
        return this.dnsMapper.selectObjByPrimaryDomain(primaryDomain);
    }

    @Override
    public boolean save(Dns instance) {
        if (instance.getId() == null) {
            instance.setAddTime(new Date());
        }
        if (instance.getId() == null) {
            try {
                this.dnsMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                this.dnsMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(Dns instance) {
        try {
            this.dnsMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.dnsMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String get() {
//        String path = Global.PYPATH + "getdns.py";
//        String result = pythonExecUtils.exec(path);
        String result = SNMPv2Request.getDnsSettings();
        return result;
    }

    @Override
    public String modifydns(String[] params) {
//        String path = Global.PYPATH + "modifydns.py";
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPv2Request.modifyDns(params[0], params[1]);
        return result;
    }
}
