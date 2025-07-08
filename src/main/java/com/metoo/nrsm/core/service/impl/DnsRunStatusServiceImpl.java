package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.DnsRunStatusMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.IDnsRunStatusService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.utils.py.ssh.SshExec;
import com.metoo.nrsm.entity.DnsRunStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 10:11
 */
@Service
@Transactional
public class DnsRunStatusServiceImpl implements IDnsRunStatusService {

    @Autowired
    private DnsRunStatusMapper dnsRunStatusMapper;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Override
    public DnsRunStatus selectOneObj() {
        return this.dnsRunStatusMapper.selectOneObj();
    }

    @Override
    public boolean update(DnsRunStatus install) {
        try {
            this.dnsRunStatusMapper.update(install);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkdns() {
//        String path = Global.PYPATH + "checkdns.py";
//        String result = pythonExecUtils.exec(path);
        String result = SNMPv2Request.checkdhcpd("Dnsredis");
        if ("None".equals(result)) {
            return false;
        } else {
            return Boolean.valueOf(result);
        }
    }

    @Override
    public void start() {
//        String path = Global.PYPATH + "Dnsredis.py";
//        String[] params = {"0", "&"};
//        String result = PythonExecUtils.execNohup(path, params, "nohup");
        String command = "/opt/autostart/Dnsredis.sh start 0";
        SshExec.exec(command);
    }

    @Override
    public void stop() {
//        String path = Global.PYPATH + "Processkill.py";
//
//        String param_path = Global.PYPATH + "Dnsredis.py";
//
//        String[] params = {"\"^python3 " + param_path + "\\b\""};
//
//        String result = PythonExecUtils.exec(path, params);
        String command = "/opt/autostart/Dnsredis.sh stop";
        SshExec.exec(command);
    }
}
