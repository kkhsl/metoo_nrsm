package com.metoo.nrsm.core.config.utils;

import com.metoo.nrsm.core.vo.Result;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {


    public static Result unlogin() {
        return result(401, "Log in");
    }

    public static Result unauthz() {
        return result(403, "Insufficient authority");
    }

    public static Result expired() {
        return result(4011, "Login expired");
    }

    public static Result badArgumentValue() {
        return result(402, "参数错误");
    }

    public static Result nullPointException() {
        return result(402, "参数错误");
    }

    public static Result arithmeticException() {
        return result(402, "Data exception");
    }

    public static Result resourceNotFound() {
        return fail(400, "not found");
    } //未找到指定资源

    public static Result serious() {
        return result(502, "系统异常");
    }

    public static Result httpRequestMethodNotSupportedException() {
        return new Result(405, "Method Not Allowed");
    }

    public static Result fileNotFoundException() {
        return new Result(1, "fileNotFound");
    }

    public static Result add() {
        return new Result(200, "Successfully added");
    }

    public static Result badArgument() {
        return fail(400, "参数错误");
    }

    public static Result dataNotFound() {
        return new Result(1, "Data Not Found");
    }

    public static Result badArgument(String message) {
        return fail(400, message);
    } //未找到指定资源

    public static Result saveError() {
        return fail(500, "保存失败");
    }

    public static Result deleteError() {
        return fail(500, "删除失败");
    }

    public static Result badArgumentRepeatedName() {
        return fail(400, "名称重复");
    } //未找到指定资源

    public static Result badArgument(int code, String message) {
        return fail(code, message);
    }

    public static Result update() {
        return new Result(200, "Modify successfully");
    }

    public static Result delete() {
        return new Result(200, "Successfully delete");
    }

    public static Result query(Map data) {
        return new Result(200, "Successfully query", data);
    }

    public static Result query() {
        return new Result(200, "Successfully query");
    }

    public static Result prohibitDel() {
        return new Result(501, "Cannot delete, delete the associated item first");
    }

    public static Result error() {
        return new Result(500, "系统错误");
    }

    public static Result error(String message) {
        return new Result(500, message);
    }

    public static Result error(int code, String message) {
        return new Result(500, message);
    }

    public static Result fail(String data) {
        return new Result(500, data);
    }

    public static Result ok() {
        return new Result(200, "Successfully");
    }

    public static Result ok(Object data) {
        return new Result(200, "Successfully", data);
    }

    public static Object notFound() {
        return new Result(404, "请求的资源不存在");
    }


    public static Object missingparameter() {
        return new Result(400, "缺少必填参数");
    }

    public static Object InvalidFormatException() {
        return new Result(400, "数据格式异常");
    }

    public static Object updatedDataFailed() {
        return new Result(500, "操作失败");
    }


    public static Result noContent() {
        return new Result(204, "Successfully");
    }

    public static Result fail(int errno, String errmsg) {
        Result obj = new Result(errno, errmsg);
        return obj;
    }

    public static Result result(int errno, String errmsg) {
        Result obj = new Result(errno, errmsg);
        return obj;
    }
}
