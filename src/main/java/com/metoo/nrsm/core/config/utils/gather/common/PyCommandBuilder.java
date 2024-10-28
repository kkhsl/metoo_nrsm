package com.metoo.nrsm.core.config.utils.gather.common;


import com.metoo.nrsm.core.utils.Global;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class PyCommandBuilder {

    // 链式调用方法连续设置多个属性值
    @ApiModelProperty("前缀")
    private String prefix; // 可以设置flag，决定是否使用前缀

    @ApiModelProperty("python执行命令版本，可以替换为其他命令或选择为空")
    private String version;

    @ApiModelProperty("文件绝对路径")
    private String path;

    private String py_prefix;

    @ApiModelProperty("文件")
    private String name;

    @ApiModelProperty("文件执行所需参数")
    private String[] params; // 改为可变长属性

    private List<String> params2 = new ArrayList();

    public PyCommandBuilder() {
    }

//    public static void main(String[] args) {
//        PyCommandBuilder pyCommandBuilder = new PyCommandBuilder()
//                .prefix("nohup")
//                .name("java -jar netmap.jar")
//                .params(new String[]{"abc", "def"})
//                .addParams2("ghl")
//                .addParams2("ljk");
//        System.out.println(pyCommandBuilder.toString());
//
//        String command = pyCommandBuilder.toParamsString();
//        System.out.println("命令行：" + command);
//    }

    public static void main(String[] args) {

        PyCommandBuilder pyCommand = new PyCommandBuilder();
        pyCommand.setVersion("py.exe");
        pyCommand.setPy_prefix("-W ignore");
        pyCommand.setPath("C:\\\\netmap\\\\script");
        pyCommand.setName("main.py");
//        pyCommand.setParams(new String[]{
//                "h3c",
//                "switch",
//                "192.168.100.1",
//                "ssh",
//                "22",
//                "metoo",
//                "metoo89745000", Global.PY_SUFFIX_GET_FIREWALL});
        String[] command = pyCommand.toStringArray();
        System.out.println(command);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPy_prefix() {
        return py_prefix;
    }

    public void setPy_prefix(String py_prefix) {
        this.py_prefix = py_prefix;
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

    public PyCommandBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public PyCommandBuilder py_prefix(String py_prefix) {
        this.py_prefix = py_prefix;
        return this;
    }


    public PyCommandBuilder version(String version) {
        this.version = version;
        return this;
    }

    public PyCommandBuilder path(String path) {
        this.path = path;
        return this;
    }

    public PyCommandBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PyCommandBuilder params(String[] params) {
        this.params = params;
        return this;
    }

    public PyCommandBuilder addParams2(String params) {
        this.params2.add(params);
        return this;
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

        if (params2 != null && params2.size() > 0) {
            for (String param : params2) {
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


    public String[] toStringArray() {
        List<String> list = new ArrayList<>();
        if (prefix != null && !prefix.isEmpty()) {
            list.add(prefix);
        }
        if (version != null && !version.isEmpty()) {
            list.add(version);
        }


        if (py_prefix != null && !py_prefix.isEmpty()) {
            list.add(py_prefix);
        }
//        // 改为cd到文件所在目录
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
        if (params2 != null && params2.size() > 0) {
            for (String param : params2) {
                if (param != null && !param.isEmpty()) {
                    list.add(param);
                }
            }
        }
        // Convert list to array
        String[] array = new String[list.size()];
        return list.toArray(array);
    }


}
