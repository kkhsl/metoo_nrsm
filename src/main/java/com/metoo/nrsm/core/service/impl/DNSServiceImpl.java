package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.service.IDNSService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import org.springframework.stereotype.Service;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-29 16:09
 */
@Service
public class DNSServiceImpl implements IDNSService {

    @Override
    public String get() {
        String path = Global.PYPATH + "getdns.py";
        String result = PythonExecUtils.exec(path);
        return result;
    }

    @Override
    public String modifydns(String[] params) {
        String path = Global.PYPATH + "modifydns.py";
        String result = PythonExecUtils.exec(path, params);
        return result;
    }
}
