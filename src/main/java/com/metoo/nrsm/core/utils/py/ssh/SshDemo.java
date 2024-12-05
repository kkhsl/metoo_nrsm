package com.metoo.nrsm.core.utils.py.ssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SshDemo {

    public static void main(String[] args) throws IOException {
//        String[] str = {
//                "/opt/flow_monitor/py/gettraffic.py"
//                ,"--type"
//                ,"h3c"
//                ,"--vendor"
//                ,"h3c"
//                ,"--command"
//                ,"get_ipv4_port"
//                ,"--dhost"
//                ,"202.103.100.254"
//                ,"--version"
//                ,"v2c"
//                ,"--community"
//                ,"transfar@123"
//                ,"--ip"
//                ,"172.16.253.253"
//                ,"--oid"
//                ,"'1.3.6.1.2.1.4.20.1.2'"};
//        System.out.println(exec(str));;
        // python3 gettraffic.py --type h3c --vendor h3c --command get_ipv4_port --dhost 202.103.100.254
        // --version v2c --community transfar@123 --ip 172.16.253.253 --oid '1.3.6.1.2.1.4.20.1.2'

        String[] str = {
                "--version"};
        System.out.println(exec(str));;
    }

    public static String exec(String[] params) throws IOException {
//        String host = "175.6.37.154";
//        int port = 22;
//        String username = "root";
//        String password = "Metoo89745000!";

        String host = "192.168.6.101";
        int port = 22;
        String username = "metoo";
        String password = "metoo89745000";

        Session session = null;

        // 创建连接
        Connection conn = new Connection(host, port);

        conn.setTCPNoDelay(true);  // 禁用延迟

        // 启动连接
        try {
            conn.connect();
            // 验证用户密码
            conn.authenticateWithPassword(username, password);
            session = conn.openSession();
            String py_version = "python3";

//            if ("dev".equals(env)) {
//                py_version = "python";
//            }

            String[] args = new String[]{
                    py_version};

            if (params.length > 0) {
                String[] mergedArray = new String[args.length + params.length];

                int argsLen = args.length;

                for (int i = 0; i < mergedArray.length; i++) {

                    if (i < argsLen) {
                        mergedArray[i] = args[i];
                    } else {
                        mergedArray[i] = params[i - argsLen];
                    }
                }
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String str : mergedArray) {
                        sb.append(str).append(" ");
                    }
                    session.execCommand(sb.toString().trim());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String str : args) {
                        sb.append(str).append(" ");
                    }
                    session.execCommand(sb.toString().trim());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 消费所有输入流
            String inStr = consumeInputStream(session.getStdout());
            return inStr;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(session != null){
                session.close();
            }
            if(conn != null){
                conn.close();
            }
        }
        return "";
    }

    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s ;
        StringBuilder sb = new StringBuilder();
        while((s=br.readLine())!=null){
            sb.append(s);
        }
        return sb.toString();
    }
}
