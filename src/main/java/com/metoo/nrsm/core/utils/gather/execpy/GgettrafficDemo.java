package com.metoo.nrsm.core.utils.gather.execpy;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.core.utils.py.ssh.SSHExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 12:40
 */
@Component
public class GgettrafficDemo {


    public static void main(String[] args) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {"192.168.5.51", "v2c",
                "public@123", "1.3.6.1.2.1.31.1.1.1.6.770", "1.3.6.1.2.1.31.1.1.1.10.770"};

        SSHExecutor sshExecutor = new SSHExecutor();

        String result = sshExecutor.exec(path, params);
        if (StringUtil.isNotEmpty(result)) {
            System.out.println(result);
        }
    }

    @Autowired
    private IFluxConfigService fluxConfigService;


    public String test(String ip, String in, String out) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {ip, "v2c",
                "public@123", in, out};
        SSHExecutor sshExecutor = new SSHExecutor();
        String result = sshExecutor.exec(path, params);
        if (StringUtil.isNotEmpty(result)) {
            return result;
        }
        return null;
    }

}
