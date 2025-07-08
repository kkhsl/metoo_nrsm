package com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.utils.Global;
import lombok.extern.slf4j.Slf4j;

/**
 * 定义并发执行cf-scanner
 */
@Slf4j
public class CFScanner implements Runnable {

    private String cidr;

    private static final String SCANNER_MODE = "ping";
    private static final boolean SKIP_NS = true;
    private static final boolean SKIP_PING = false;


    public CFScanner(String cidr) {
        this.cidr = cidr;
    }

    @Override
    public void run() {
        try {
            PyCommandBuilder3 pyCommand = new PyCommandBuilder3();
            pyCommand.setPath(Global.cf_scanner_path);
            pyCommand.setName(Global.cf_scanner_name);
            pyCommand.setParams(new String[]{
                    "-i", cidr,
                    "-ns=" + SKIP_NS,
                    "-np=" + SKIP_PING,
                    "-m", SCANNER_MODE
            });
            String result = PyExecUtils.execCFScanner(pyCommand);
            log.info("CF-Scanner 扫描 {} 结果：{}", cidr, result);
        } catch (Exception e) {
            log.error("执行异常", e);
            e.printStackTrace();
        }
    }

}
