package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.PingIpConfigMapper;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.service.IPingIpConfigService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.PingIpConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PingIpConfigServiceImpl implements IPingIpConfigService {

    @Autowired
    private PingIpConfigMapper pingIpConfigMapper;
    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Override
    public PingIpConfig selectOneObj() {
        return this.pingIpConfigMapper.selectOneObj();
    }

    @Override
    public boolean update(PingIpConfig install) {
        try {
            PingIpConfig config = this.pingIpConfigMapper.selectOneObj();
            if (config == null) {
                this.pingIpConfigMapper.save(install);
            }
            this.pingIpConfigMapper.update(install);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Result save(PingIpConfig newConfig) {
        PingIpConfig dbConfig = this.pingIpConfigMapper.selectOneObj();
        int i = 0;
        // 尝试并发写入
        if (dbConfig == null) {
            i = this.pingIpConfigMapper.save(newConfig);
        }
        newConfig.updateStatusIfChanged(dbConfig);
        if (newConfig.getStatus() != null && newConfig.getStatus() == 1) {
            i = this.pingIpConfigMapper.update(newConfig);
        }
        if (i > 0) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.error();
        }
    }

    @Override
    public boolean checkaliveip() {
//        String path = Global.PYPATH + "checkping.py";
//        String result = pythonExecUtils.exec(path);
        String result = SNMPv2Request.checkdhcpd("checkaliveip");
        if ("None".equals(result)) {
            return false;
        }
        return Boolean.valueOf(result);
    }

    @Override
    public boolean status() {
//        String path = Global.PYPATH + "pingop.py";
        String[] params = {"status", "checkaliveip"};
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPv2Request.pingOp(params[0], params[1]);
        log.info("=========status" + params);
        return Boolean.valueOf(result);
    }

    @Override
    public boolean start() {
//        String path = Global.PYPATH + "pingop.py";
        String[] params = {"start", "checkaliveip"};
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPv2Request.pingOp(params[0], params[1]);
        return Boolean.valueOf(result);
    }

    @Override
    public boolean stop() {
//        String path = Global.PYPATH + "pingop.py";
        String[] params = {"stop", "checkaliveip"};
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPv2Request.pingOp(params[0], params[1]);
        return Boolean.valueOf(result);
    }

    @Override
    public boolean restart() {
//        String path = Global.PYPATH + "pingop.py";
        String[] params = {"restart", "checkaliveip"};
//        String result = pythonExecUtils.exec(path, params);
        String result = SNMPv2Request.pingOp(params[0], params[1]);
        return Boolean.valueOf(result);
    }
}
