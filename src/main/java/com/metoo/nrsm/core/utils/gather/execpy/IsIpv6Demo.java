package com.metoo.nrsm.core.utils.gather.execpy;

import com.metoo.nrsm.core.utils.py.ssh.SSHExecutor;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 15:11
 */
public class IsIpv6Demo {

    public static void main(String[] args) {
        String path = "/opt/nrsm/py/isipv6.py";
        String[] params = {"192.168.5.51", "v2c",
                "public@123"};
        SSHExecutor sshExecutor = new SSHExecutor();
        String result = sshExecutor.exec(path, params);
        if(Boolean.valueOf(result)){
            System.out.println(true);
        }else{
            System.out.println(false);
        }
    }
}
