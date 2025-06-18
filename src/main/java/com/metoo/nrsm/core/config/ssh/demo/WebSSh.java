package com.metoo.nrsm.core.config.ssh.demo;


import com.metoo.nrsm.core.utils.ip.Ipv4Util;

public class WebSSh {

    public static void main(String[] args) {
        String pwd = "abcdefghijklmnopqrstuvwxyz";
        if (pwd.length() > 10) {
            pwd = pwd.substring(3);
            pwd = pwd.substring(0, pwd.length() - 7);
            System.out.println(pwd);
        }

        String ip = "192.168.5.101";
        boolean ipv4 = Ipv4Util.isIp4(ip);
        System.out.println(ipv4);

        String ip4 = Ipv4Util.getMatcherIP4(ip);
        System.out.println(ip4);
    }

}
