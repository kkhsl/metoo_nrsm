package com.metoo.nrsm.core.config.utils;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.entity.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiroUserHolder {

    @Autowired
    private IUserService userService;

    public static User currentUser() {
        if (SecurityUtils.getSubject() != null){
            Subject subject = SecurityUtils.getSubject();
            if(subject.getPrincipal() != null && subject.isAuthenticated()){
                String userName = SecurityUtils.getSubject().getPrincipal().toString();
                IUserService userService = (IUserService) ApplicationContextUtils.getBean("userServiceImpl");
                  User user = userService.findByUserName(userName);
                if(user != null){
                    return user;
                }
            }
        }

        return null;
    }

}
