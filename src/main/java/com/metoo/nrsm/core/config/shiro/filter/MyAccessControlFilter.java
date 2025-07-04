package com.metoo.nrsm.core.config.shiro.filter;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.vo.Result;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MyAccessControlFilter extends AccessControlFilter {

    @Value("${spring.profiles.active}")
    private String env;

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        //判断用户是通过记住我功能自动登录,此时session失效

        Subject subject = SecurityUtils.getSubject();
        if (subject.getPrincipal() != null) {
            // 如果未认证并且未IsreMenmberMe(Session失效问题)
            if (subject.isAuthenticated()/* || subject.isRemembered()*/) {
                return true;
            } else {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print(JSONObject.toJSONString(new Result(401, "Log in")));
                return false;
            }
        }

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().print(JSONObject.toJSONString(new Result(401, "Log in")));
        return false;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        return false;
    }
}

