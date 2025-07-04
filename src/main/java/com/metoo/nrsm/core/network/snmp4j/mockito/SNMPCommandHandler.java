package com.metoo.nrsm.core.network.snmp4j.mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class SNMPCommandHandler {

    private final SNMPClient snmp = new SNMPClient();

    // 用于存储 vendor 和 command 对应的方法名
    private final Map<String, String> methodMappings = new HashMap<>();

    // 配置文件中指定的前缀
    private static final String CONFIG_PREFIX = "snmp.";

    // 通过 @Value 注解将配置文件中的数据注入到类中
    @Value("${snmp.h3c.test}")
    private String h3cTestMethod;


    public void handleCommand(String vendor, String command, String dhost, String version, String community, String oid) throws Exception {
        // 通过 vendor 和 command 读取对应的方法名
        String methodName = getMethodName2(vendor, command);

        if (methodName != null) {
            // 使用反射调用对应的方法
            Method method = SNMPClient.class.getMethod(methodName, String.class, String.class, String.class, String.class);
            Object result = method.invoke(snmp, dhost, version, community, oid);
            System.out.println(result);
        } else {
            System.out.println("无效的命令。");
        }
    }

    /**
     * 动态获取方法名
     * 根据 vendor 和 command 获取对应的 SNMP 方法名
     *
     * @param vendor  设备厂商
     * @param command 要执行的命令
     * @return 对应的 SNMP 方法名
     */
    private String getMethodName(String vendor, String command) {
        // 根据 vendor 和 command 动态构建配置文件中的键
        String key = CONFIG_PREFIX + vendor + "." + command;

        try {
            // 使用反射来动态获取配置文件中对应的值
            return (String) this.getClass().getDeclaredField(key).get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;  // 如果没有找到对应的字段，返回 null
        }
    }

    @Autowired
    private Environment environment;

    public String getMethodName2(String vendor, String command) {
        // 动态拼接配置键
        String key = CONFIG_PREFIX + vendor + "." + command;

        // 从配置文件中获取值
        String value = environment.getProperty(key);

        if (value == null) {
            System.out.println("未找到对应的配置项: " + key);
            return null;  // 如果没有找到对应的配置项，返回 null
        }

        return value;  // 返回对应的配置值
    }
}
