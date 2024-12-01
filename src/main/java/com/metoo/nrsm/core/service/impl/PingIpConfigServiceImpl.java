package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.PingIpConfigMapper;
import com.metoo.nrsm.core.service.IPingIpConfigService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.PingIpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:34
 */
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
            this.pingIpConfigMapper.update(install);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkaliveip() {
        String path = Global.PYPATH + "checkping.py";
        String result = pythonExecUtils.exec(path);
        if("None".equals(result)){
            return false;
        }
        return Boolean.valueOf(result);
    }

    @Override
    public boolean start() {
        String path = Global.PYPATH + "pingop.py";
        String[] params = {"start", "checkaliveip"};
        String result = pythonExecUtils.exec(path, params);
        return Boolean.valueOf(result);
    }

    @Override
    public boolean stop() {
        String path = Global.PYPATH + "pingop.py";
        String[] params = {"stop", "checkaliveip"};
        String result = pythonExecUtils.exec(path, params);
        return Boolean.valueOf(result);
    }

    @Override
    public boolean restart() {
        String path = Global.PYPATH + "pingop.py";
        String[] params = {"restart", "checkaliveip"};
        String result = pythonExecUtils.exec(path, params);
        return Boolean.valueOf(result);
    }
}
