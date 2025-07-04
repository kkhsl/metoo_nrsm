package com.metoo.nrsm.core.config.shiro;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.shiro.salt.MyByteSource;
import com.metoo.nrsm.core.service.IResService;
import com.metoo.nrsm.core.service.IRoleService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.entity.Res;
import com.metoo.nrsm.entity.Role;
import com.metoo.nrsm.entity.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 自定义Realm 将认证/授权数据来源设置为数据库的实现
 */
public class MyRealm extends AuthorizingRealm {

    @Autowired
    private IRoleService roleService;
    @Autowired
    private IResService resService;

    /**
     * 限定这个 Realm 只处理 UsernamePasswordToken
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String username = (String) principalCollection.getPrimaryPrincipal();
        System.out.println("userName：" + username);
        IUserService userService = (IUserService) ApplicationContextUtils.getBean("userServiceImpl");
        User user = userService.findByUserName(username);
        List<Role> roles = this.roleService.findRoleByUserId(user.getId());//user.getRoles();
        if (!CollectionUtils.isEmpty(roles)) {
            if (user != null) {
                SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
                for (Role role : roles) {
                    simpleAuthorizationInfo.addRole(role.getRoleCode());
                    List<Res> permissions = resService.findResByRoleId(role.getId());
                    if (!CollectionUtils.isEmpty(permissions)) {
                        permissions.forEach(permission -> {
                            simpleAuthorizationInfo.addStringPermission(permission.getValue());
                        });
                    }
                }
                return simpleAuthorizationInfo;
            }
        }
        return null;
    }

    // 认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String username = (String) authenticationToken.getPrincipal();
        IUserService userService = (IUserService) ApplicationContextUtils.getBean("userServiceImpl");

        User user = userService.findByUserName(username);
        if (!ObjectUtils.isEmpty(user)) {
            if (username.equals(user.getUsername())) {
                String userName = user.getUsername();
                return new SimpleAuthenticationInfo(userName, user.getPassword(), new MyByteSource(user.getSalt()), this.getName());
            }
        }
        return null;
    }

}
