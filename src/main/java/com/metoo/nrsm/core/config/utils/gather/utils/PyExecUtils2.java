package com.metoo.nrsm.core.config.utils.gather.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.gather.Process.PythonScriptRunner;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommand;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder3;
import com.metoo.nrsm.core.config.utils.gather.ssh.SSHUtils;
import com.metoo.nrsm.core.utils.Global;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 12:00
 */
@Slf4j
@Component
public class PyExecUtils2 {

    @Autowired
    private SSHUtils sshUtils;
    @Autowired
    private PythonScriptRunner pythonScriptRunner;

    public String exec(PyCommand pyCommand) {
        String result = this.sshUtils.executeCommand(pyCommand.toParamsString());
        return result;
    }

    public String exec(PyCommandBuilder3 pyCommand) {
        String result = "";
        if ("dev".equals(Global.env)) {
            result = this.sshUtils.executeCommand(pyCommand.toParamsString());

            log.info("command: " + pyCommand.toParamsString() + "result【" + result + "】end");


        }else {
            result = this.pythonScriptRunner.exec(pyCommand.getPath(), pyCommand.toStringArray());

            log.info("command: " + pyCommand.toParamsString() + "result【" + result + "】end");

        }

        return result;
    }

    public static boolean isJsonObject(String jsonString) {
        try {
            // 尝试解析字符串
            JSONObject.parseObject(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isJsonArray(String jsonString) {
        try {
            // 尝试解析字符串
            JSONArray.parseObject(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String execPy(PyCommandBuilder3 pyCommand) {
        String result = "";
        result = PythonScriptRunner.execPy(pyCommand.getPath(), pyCommand.toStringArray());
        return result;
    }




}
