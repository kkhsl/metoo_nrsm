package com.metoo.nrsm.core.vo;


import java.io.Serializable;
import java.util.HashMap;

/**
 * Result 类继承自 HashMap<String, Object>，这意味着 Result 类本质上是一个 Map。
 * 当 Spring MVC 遇到这种对象时，它会自动将其转换为 JSON 格式响应，而不需要显式加上 @ResponseBody 注解
 */
public class Result /*extends HashMap<String, Object>*/ implements Serializable {


    private static final long serialVersionUID = 4267799476339238113L;
    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private Object data;

//    private User user;
//
//    public User getUser(){return user;}
//    public void setUser(User user){
//        this.user=user;
//    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}