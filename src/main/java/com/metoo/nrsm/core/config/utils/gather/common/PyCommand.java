package com.metoo.nrsm.core.config.utils.gather.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 10:16
 */
@ApiModel("执行Python脚本参数")
@Component
public class PyCommand {

    // 链式调用方法连续设置多个属性值

    @ApiModelProperty("前缀")
//    @Value("${py.prefix}")
    private String prefix; // 可以设置flag，决定是否使用前缀

    //    @Value("${py.version}")
    private String version;

    //    @Value("${py.path}")
    @ApiModelProperty("文件绝对路径")
    private String path;

    @ApiModelProperty("文件")
    private String name;

    @ApiModelProperty("文件执行所需参数")
    private String[] params; // 改为可变长属性

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public PyCommand() {
    }

    public PyCommand(String path, String[] params) {
        this.path = path;
        this.params = params;
    }


    // 方法：将对象的属性值转换为字符串数组
//    public String[] toStringArray() {
//        return new String[]{prefix, version, path, params};
//    }

    // 将对象的属性值转换为字符串数组，包括params
    public String[] toStringArray() {
//        String[] array = new String[5]; // 4 是属性的数量
//        array[0] = prefix;
//        array[1] = version;
//        array[2] = path;
//        array[3] = name;
//        array[4] = params != null ? String.join(",", params) : "";
//        return array;

//        String[] array = new String[5];
//        array[0] = prefix != null ? prefix : "";
//        array[1] = version != null ? version : "";
//        array[2] = path != null ? path : "";
//        array[3] = name;
//        array[4] = params != null ? Arrays.toString(params) : "";
//
//        return array;

        List<String> list = new ArrayList<>();
        if (prefix != null && !prefix.isEmpty()) {
            list.add(prefix);
        }
        if (version != null && !version.isEmpty()) {
            list.add(version);
        }
        // 改为cd到文件所在目录
//        if (path != null && !path.isEmpty()) {
//            if (name != null && !name.isEmpty()) {
//                list.add(path + name);
//            }else{
//                list.add(path);
//            }
//        }
        if (name != null && !name.isEmpty()) {
            list.add(name);
        }
        if (params != null) {
            for (String param : params) {
                if (param != null && !param.isEmpty()) {
                    list.add(param);
                }
            }
        }
        // Convert list to array
        String[] array = new String[list.size()];
        return list.toArray(array);
    }

    public List toArray() {

        List<String> list = new ArrayList<>();
        if (prefix != null && !prefix.isEmpty()) {
            list.add(prefix);
        }
        if (version != null && !version.isEmpty()) {
            list.add(version);
        }
//        if (path != null && !path.isEmpty()) {
//            if (name != null && !name.isEmpty()) {
//                list.add(path + name);
//            }else{
//                list.add(path);
//            }
//        }
        if (name != null && !name.isEmpty()) {
            list.add(name);
        }
        if (params != null) {
            for (String param : params) {
                if (param != null && !param.isEmpty()) {
                    list.add(param);
                }
            }
        }
        // Convert list to array
        return list;
    }

    public String toParamsString() {

        List<String> list = new ArrayList<>();

        if (prefix != null) {
            list.add(prefix);
        }

        if (path != null) {
            String path1 = " cd " + path + " && ";
            list.add(path1);
        }

        if (version != null) {
            list.add(version);
        }
//        if (path != null && !path.isEmpty()) {
//            if (name != null && !name.isEmpty()) {
//                list.add(path + name);
//            }else{
//                list.add(path);
//            }
//        }
        if (name != null && !name.isEmpty()) {
            list.add(name);
        }
        if (params != null) {
            for (String param : params) {
                if (param != null && !param.isEmpty()) {
                    list.add(param);
                }
            }
        }
        // Convert list to array
        String[] array = new String[list.size()];

        String result = String.join(" ", list.toArray(array));

        return result;
    }

    public static void main(String[] args) {
        // python3 /opt/sqlite/controller/main.py h3c switch 192.168.100.1 ssh 22 metoo metoo89745000 aliveint
        PyCommand command = new PyCommand();
//        command.setPrefix("nohup");
//        command.setVersion("python3");
        command.setPath("/opt/sqlite/controller/");
        command.setName("main.py");
        command.setParams(new String[]{"h3c", "switch", "192.168.100.1", "ssh", "22",
                "metoo", "metoo89745000", "aliveint"});
        String result = command.toParamsString();
        System.out.println(result); // 打印字符串数组

        String[] result2 = command.toStringArray();
        System.out.println(result2); // 打印字符串数组
    }

    // Fluent API 或者 Builder 模式

}
