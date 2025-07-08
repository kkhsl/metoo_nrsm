package com.metoo.nrsm.core.config.utils.gather.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.utils.Global;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-05 14:59
 */
public class Ssh2ExecUtils {


    // 方法重载：接收可变数量的字符串参数的方法
    public void process(String... strs) {
        System.out.print("Processing strings:");
        for (String str : strs) {
            System.out.print(" " + str);
        }
        System.out.println();
    }

    @Test
    public void test() {
        this.process("a", "b");
    }

    public static String exec() {

        return "";
    }


    public static String exec(String path) {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";

        Session session = null;

        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        try {
            conn.connect();
            // 验证用户密码
            conn.authenticateWithPassword(username, password);
            session = conn.openSession();
            String py_version = "python3";

            String[] args = new String[]{
                    py_version, path};

            try {
                StringBuilder sb = new StringBuilder();
                for (String str : args) {
                    sb.append(str).append(" ");
                }
                session.execCommand(sb.toString().trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 消费所有输入流
            String inStr = consumeInputStream(session.getStdout());
            return inStr;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return "";
    }

    public static String exec(String path, String[] params) {
        String host = "192.168.5.101";
        int port = 22;
        String username = "root";
        String password = "metoo89745000";

        Session session = null;

        // 创建连接
        Connection conn = new Connection(host, port);
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
                    py_version, path};

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
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return "";
    }

    // 获取连接
    public Session getSession() {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";

        // 创建连接
        Connection conn = new Connection(host, port);
        try {
            conn.connect();
            // 验证用户密码
            conn.authenticateWithPassword(username, password);
            Session session = conn.openSession();
            return session;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String exec(String path, String[] params, String prefix) {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";

        Session session = null;

        // 创建连接
        Connection conn = new Connection(host, port);
        // 启动连接
        try {
            conn.connect();
            // 验证用户密码
            conn.authenticateWithPassword(username, password);
            session = conn.openSession();
            String py_version = "python3";

            String[] args = null;
            if (StringUtil.isNotEmpty(prefix)) {
                args = new String[]{
                        prefix, py_version, path};
            } else {
                args = new String[]{
                        py_version, path};
            }

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
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return "";
    }

    public String getParams(String prefix, String path, String[] params) {
        String py_version = "python3";

        String[] args = null;

        if (StringUtil.isNotEmpty(prefix)) {
            args = new String[]{prefix, py_version, path};
        } else {
            args = new String[]{py_version, path};
        }

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
            StringBuilder sb = new StringBuilder();
            for (String str : mergedArray) {
                sb.append(str).append(" ");
            }
            return sb.toString().trim();
        }
        return "";
    }


    public static String execNohup(String path, String[] params, String prefix) {
        String host = "192.168.5.205";
        int port = 22;
        String username = "nrsm";
        String password = "metoo89745000";

        Session session = null;

        // 创建连接
        Connection conn = new Connection(host, port);
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

            String[] args = null;
            if (StringUtil.isNotEmpty(prefix)) {
                args = new String[]{
                        prefix, py_version, path};
            } else {
                args = new String[]{
                        py_version, path};
            }

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
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return "";
    }


    public String exec2(String path, String[] params) {
        Session session = getSession();
        String py_version = "python";

        if (Global.env.equals("prod")) {
            py_version = Global.py_name;
        }

        String[] args = new String[]{
                py_version, path};

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
                session.execCommand(mergedArray.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                session.execCommand(args.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 消费所有输入流
        String inStr = null;
        try {
            inStr = consumeInputStream(session.getStdout());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inStr;
    }

    /**
     * 消费inputstream，并返回
     */
    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }
}
