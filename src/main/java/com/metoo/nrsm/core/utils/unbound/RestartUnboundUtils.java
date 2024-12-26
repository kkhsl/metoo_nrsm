package com.metoo.nrsm.core.utils.unbound;

import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.command.CommandExecutor;
import com.metoo.nrsm.core.utils.command.CommandExecutorSsh;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RestartUnboundUtils {

    // 重新启动 Unbound 服务
    public static boolean restartUnboundService() {
        try {
            // 执行系统命令来重启 unbound 服务
            String command = "sudo systemctl restart unbound";
            String result = "";
            if ("dev".equals(Global.env)) {
                result = CommandExecutorSsh.execCommand("sudo systemctl restart unbound");

            } else {
                result = CommandExecutor.executeCommand(command);  // 执行命令
            }
//            if ("".contains(restart)) {
//                System.out.println("启动成功");
//            } else {
//                System.out.println("启动失败");
//            }
//            String status = CommandExecutorSsh.exec("sudo systemctl status unbound");
            if ("".contains(result)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
//            return "Error restarting unbound service: " + e.getMessage();  // 返回错误信息
            return false;
        }
    }

    public static void main(String[] args) {
        boolean result = restartUnboundService();
        System.out.println(result);  // 打印结果或错误
    }

}
