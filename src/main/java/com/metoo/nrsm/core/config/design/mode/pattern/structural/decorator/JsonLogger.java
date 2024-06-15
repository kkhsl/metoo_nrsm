package com.metoo.nrsm.core.config.design.mode.pattern.structural.decorator;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-29 16:35
 *
 * @title 装饰器模式（结构型模式）
 *
 * 具体装饰器: Json-logger
 */
public class JsonLogger extends DecoratorLogger{

    public JsonLogger(Logger logger) {
        super(logger);
    }

    @Override
    public void error(String message) {
        JSONObject obj = new JSONObject();
        obj.put("message", message);
        logger.error(obj.toString());
    }


    @Override
    public void info(String message) {
        JSONObject obj = new JSONObject();
        obj.put("message", message);
        logger.info(obj.toString());
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String s) {

    }

    @Override
    public void trace(String s, Object o) {

    }

    @Override
    public void trace(String s, Object o, Object o1) {

    }

    @Override
    public void trace(String s, Object... objects) {

    }

    @Override
    public void trace(String s, Throwable throwable) {

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String s) {

    }

    @Override
    public void trace(Marker marker, String s, Object o) {

    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {

    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {

    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String s) {

    }

    @Override
    public void debug(String s, Object o) {

    }

    @Override
    public void debug(String s, Object o, Object o1) {

    }

    @Override
    public void debug(String s, Object... objects) {

    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String s) {

    }

    @Override
    public void debug(Marker marker, String s, Object o) {

    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {

    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {

    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String s, Object o) {

    }

    @Override
    public void info(String s, Object o, Object o1) {

    }

    @Override
    public void info(String s, Object... objects) {

    }

    @Override
    public void info(String s, Throwable throwable) {

    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String s) {

    }

    @Override
    public void info(Marker marker, String s, Object o) {

    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {

    }

    @Override
    public void info(Marker marker, String s, Object... objects) {

    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String s) {

    }

    @Override
    public void warn(String s, Object o) {

    }

    @Override
    public void warn(String s, Object... objects) {

    }

    @Override
    public void warn(String s, Object o, Object o1) {

    }

    @Override
    public void warn(String s, Throwable throwable) {

    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String s) {

    }

    @Override
    public void warn(Marker marker, String s, Object o) {

    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {

    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {

    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String s, Object o) {

    }

    @Override
    public void error(String s, Object o, Object o1) {

    }

    @Override
    public void error(String s, Object... objects) {

    }

    @Override
    public void error(String s, Throwable throwable) {

    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String s) {

    }

    @Override
    public void error(Marker marker, String s, Object o) {

    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {

    }

    @Override
    public void error(Marker marker, String s, Object... objects) {

    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {

    }

}
