package com.metoo.nrsm.core.utils;

import com.metoo.nrsm.core.service.impl.Dhcp6ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class Global {

    private static Global global = new Global();


//    public final static String AES_KEY = "#+NPzwDvPmCJvpY@";
    public final static String AES_KEY = "@NPzwDvPmCJvpYuE";

    public final static  String BOARDCPU = "boardcpu";
    public static final String BOARDMEM = "boardmem";
    public static final String BOARDTEMP = "boardtemp";

    public static final String TOPOLOGYFILEPATH = "/opt/nrsm/resource";// 拓扑自定义分区文件存储路径

    // 数据库备份
    public static final String DBPATH = "/opt/nrsm/nrsm/resource/db";
    public static final String DBSCRIPTPATH = "/opt/nrsm/nrsm/resource/script/backup_db1.sh";
    public static final String DBSCRIPTPATH2 = "/opt/nrsm/nrsm/resource/script/backup_db2.sh";
    public static final String LICENSEPATH = "/opt/nrsm/nrsm/resource/table";
    public static final String DBNAME = "nrsm";
    public static final String DBPATHLOCAL = "C:\\Users\\Administrator\\Desktop\\backup\\db";
    public static final String DBSCRIPTPATHLOCAL = "C:\\Users\\Administrator\\Desktop\\backup\\script\\backup_db1.bat";
    public static final String DBSCRIPTPATHLOCAL2 = "C:\\Users\\Administrator\\Desktop\\backup\\script\\backup_db2.bat";
    public static final String LICENSEPATHLOCAL = "C:\\Users\\Administrator\\Desktop\\backup\\table";


//    public static final String WEBTERMINALPATH = "C:\\Users\\Administrator\\Desktop\\backup\\dblocal";
    public static final String WEBTERMINALPATH = "/opt/rnsm/service/www/deviceImg/dt";
//    public static final String DBPATH = "C:\\Users\\Administrator\\Desktop\\backup\\db";


//    public static final String TOPOLOGYFILEPATH = "C:\\Users\\Administrator\\Desktop\\新建文件夹 (3)";

//    public static String PYPATH = "/opt/nrsm/py/";

    public static String PYPATH;

    @Value("${PYPATH}")
    public void setUrl(String PYPATH) {
        Global.PYPATH = PYPATH;
    }

    public static String py_name;

    @Value("${py_name}")
    public void setPy_name(String py_name) {
        Global.py_name = py_name;
    }

    public static String py_path;

    @Value("${py_path}")
    public void setPy_path(String py_path) {
        Global.py_path = py_path;
    }

    public static String unboundPath;

    @Value("${config.unbound.path}")
    public void setUnboundPath(String unboundPath) {
        Global.unboundPath = unboundPath;
    }


    public static String os_scanner;
    @Value("${os_scanner}")
    public void setOs_scanner(String os_scanner) {
        Global.os_scanner = os_scanner;
    }
    public static String os_scanner_name;
    @Value("${os_scanner_name}")
    public void setOs_scanner_name(String os_scanner_name) {
        Global.os_scanner_name = os_scanner_name;
    }
    public static String os_scanner_result_path;
    @Value("${os_scanner_result_path}")
    public void setOs_scanner_result_path(String os_scanner_result_path) {
        Global.os_scanner_result_path = os_scanner_result_path;
    }
    public static String os_scanner_result_name;
    @Value("${os_scanner_result_name}")
    public void setOs_scanner_result_name(String os_scanner_result_name) {
        Global.os_scanner_result_name = os_scanner_result_name;
    }

    public static String encrypt_path;
    @Value("${encrypt_path}")
    public void setEncrypt_path(String encrypt_path) {
        Global.encrypt_path = encrypt_path;
    }


    public static String host;
    public static int port;
    public static String username;
    public static String password;

    @Value("${ssh.hostname}")
    public void setHost(String host) {
        Global.host = host;
    }
    @Value("${ssh.port}")
    public void setPort(int port) {
        Global.port = port;
    }
    @Value("${ssh.username}")
    public void setUsername(String username) {
        Global.username = username;
    }
    @Value("${ssh.password}")
    public void setPassword(String password) {
        Global.password = password;
    }


    public static String dhcp;
    public static String dhcp6;

    @Value("${file.dhcp}")
    public void setDhcp(String dhcp) {
        Global.dhcp = dhcp;
    }
    @Value("${file.dhcp6}")
    public void setDhcp6(String dhcp6) {
        Global.dhcp6 = dhcp6;
    }

    public static String env;

    @Value("${spring.profiles.active}")
    public void setEnv(String env) {
        Global.env = env;
    }

    public Global() {
    }

    public static Global getInstance() {
        return global;
    }

    public static final String TRAFFIC = "TRAFFIC"; // portIpv4
}
