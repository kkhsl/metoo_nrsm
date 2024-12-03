package com.metoo.nrsm.core.config.utils.gather.strategy.other;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.strategy.Context;
import com.metoo.nrsm.core.config.utils.gather.strategy.DataCollectionStrategy;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.entity.Probe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 14:49
 * <p>
 */
@Slf4j
@Component
public class OsScannerCollectionStrategy implements DataCollectionStrategy {

    private final PyExecUtils pyExecUtils;

    @Autowired
    public OsScannerCollectionStrategy(PyExecUtils pyExecUtils) {
        this.pyExecUtils = pyExecUtils;
    }

    @Override
    public void collectData(Context context) {
        try {
            Probe obj = (Probe) context.getEntity();
            if (obj != null) {
                String ip = obj.getIp_addr();
                if(StringUtil.isEmpty(ip)){
                    ip = obj.getIpv6();
                }


                PyCommandBuilder3 pyCommand = new PyCommandBuilder3();
                pyCommand.setPrefix("");
                pyCommand.setVersion("");
//                pyCommand.setPath(Global.os_scanner);
                pyCommand.setPath(context.getPath());
                pyCommand.setName(Global.os_scanner_name);
                pyCommand.setParams(new String[]{
                        "-i",
                        ip,
                        "-o",
                        obj.getPort_num(),
                        "-c",
                        "1"
                });
                log.info("os-scanner start" + " ip: "  + ip + " port " + obj.getPort_num());
                this.pyExecUtils.exec(pyCommand);
                log.info("os-scanner end");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
