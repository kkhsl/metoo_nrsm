package com.metoo.nrsm.core.config.shiro;

//import com.metoo.nrsm.core.config.global.LicenseFilter2;

import com.metoo.nrsm.core.config.filter.LicenseFilter;
import com.metoo.nrsm.core.config.shiro.filter.MyAccessControlFilter;
import com.metoo.nrsm.core.service.ILicenseService;
import com.metoo.nrsm.core.utils.Global;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>
 * Title: ShiroConfig.java
 * </p>
 *
 * <p>
 * Description: 整合Shiro框架相关的配置类; Web环境中，自动为SecurityUtil注入Securitymanagers
 * swagger-ui.html
 * </p>
 *
 * <p>
 * authen: hkk
 * </p>
 */
@Configuration
public class ShiroConfig {


    private LicenseFilter createLicenseFilter(ILicenseService licenseService) {
        return new LicenseFilter(licenseService);
    }

    // 1, 创建ShiroFilter  //负责拦截所有请求
    // 配置访问资源所需要的权限
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager, ILicenseService licenseService) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        // 1,给过滤器设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);

        /**
         * 添加自定义拦截器，重写user认证方式，处理session超时问题
         *  添加 jwt 专用过滤器，拦截除 /login 和 /logout 外的请求
         */
        HashMap<String, Filter> myFilters = new HashMap<>(16);
        myFilters.put("rmb", new MyAccessControlFilter());
        myFilters.put("licenseFilter", createLicenseFilter(licenseService));
        shiroFilterFactoryBean.setFilters(myFilters);

        // 2,配置系统受限资源
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();


//        filterChainDefinitionMap.put("/admin/dhcp/**", "anon");
//        filterChainDefinitionMap.put("/admin/address/pool/**", "anon");


        filterChainDefinitionMap.put("/notice/**", "anon");
        filterChainDefinitionMap.put("/ws/**", "anon");
        filterChainDefinitionMap.put("/file/**", "anon");
        filterChainDefinitionMap.put("/webssh", "anon");

        filterChainDefinitionMap.put("/admin/gather/**", "anon");

        filterChainDefinitionMap.put("/admin/test/**", "anon");

        filterChainDefinitionMap.put("/user/login", "anon");// 设置所有资源都受限；避免登录资源受限，设置登录为公共资源

        filterChainDefinitionMap.put("/user/register", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/web/**", "anon");

        filterChainDefinitionMap.put("/buyer/test", "anon");
        filterChainDefinitionMap.put("/idempotent", "anon");

        filterChainDefinitionMap.put("/probeNmap/uploadScanResult", "anon");

        filterChainDefinitionMap.put("/buyer/**", "anon");// 设置所有资源都受限；避免登录资源受限，设置登录为公共资源

        filterChainDefinitionMap.put("/admin/auth/401", "anon");
        filterChainDefinitionMap.put("/admin/auth/403", "anon");
        filterChainDefinitionMap.put("/admin/auth/404", "anon");

//        filterChainDefinitionMap.put("/rtmp/**", "anon");

        filterChainDefinitionMap.put("/index.jsp", "authc");// authc 请求这个资源需要认证和授权;参数可以为视图可以为路径（/index.jsp、/**、/path/*）
        filterChainDefinitionMap.put("/index/**", "authc");

        filterChainDefinitionMap.put("/monitor/**", "anon");

        filterChainDefinitionMap.put("/admin/index/nav", "anon");
        filterChainDefinitionMap.put("/admin/**", "rmb, licenseFilter");
        filterChainDefinitionMap.put("/nspm/**", "rmb, licenseFilter");

        // 放行静态资源
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/upload/**", "anon");
        filterChainDefinitionMap.put("/hls/**", "anon");
        filterChainDefinitionMap.put("/templates/**", "anon");

        //shiroFilterFactoryBean.setLoginUrl("/login.jsp");
        //shiroFilterFactoryBean.setLoginUrl("/buyer/login");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        // sshiroFilterFactoryBean.setLoginUrl("/user/login"); // 导致不断重定向


        // shiroFilterFactoryBean.setSuccessUrl("/admin/auth/index");
        shiroFilterFactoryBean.setLoginUrl("/admin/auth/401");
        shiroFilterFactoryBean.setUnauthorizedUrl("/admin/auth/403");

        // 3，配置系统公共资源
        return shiroFilterFactoryBean;
    }

    //2, 创建安全管理器 web环境中配置webSecurity
    // getDefaultWevSecurityManager(Realm realm)
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager() {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();

        // 1-1, 给安全管理器设置Realm
        defaultWebSecurityManager.setRealm(getRealm());
        // 1-2，给安全管理器设置Realms
//        List<Realm> realms = new ArrayList<Realm>();
//        realms.add(jwtRealm());
//        realms.add(getRealm());
//        defaultWebSecurityManager.setRealms(realms);
        // 2，给安全管理器设置SessionManager
        //  getDefaultWebSessionManager (isAuthenticated:false)
        defaultWebSecurityManager.setSessionManager(getDefaultSessionManager());
        // 3，给安全管理器设置RememberMeManager
//        defaultWebSecurityManager.setRememberMeManager(rememberMeManager());

        // 3.关闭shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator());
        defaultWebSecurityManager.setSubjectDAO(subjectDAO);

        return defaultWebSecurityManager;
    }

    // 3, 自定义realm
    @Bean
    public Realm getRealm() {
        MyRealm myRealm = new MyRealm();
        // 设置Realm使用hash凭证校验匹配器; 问：Realm 不设置hash凭证器会出现什么
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 设置加密算法 SHA-1、md5
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        // 设置加密次数（散列次数）
        hashedCredentialsMatcher.setHashIterations(1024);
        myRealm.setCredentialsMatcher(hashedCredentialsMatcher);

        // 开启缓存管理
//        myRealm.setCachingEnabled(true);// 开启全局缓存
        // 方式一：EhCache
//        myRealm.setCacheManager(new EhCacheManager());// EhCache
////        方式二：Redis
//        myRealm.setCacheManager(new RedisCacheManager());// RedisCacheManager
//        myRealm.setAuthenticationCachingEnabled(true);// 认证缓存
//        myRealm.setAuthenticationCacheName("authenticationCache");
//        myRealm.setAuthorizationCachingEnabled(true);// 授权缓存
//        myRealm.setAuthorizationCacheName("authorizationCache");
        return myRealm;
    }

    /**
     * 配置 jwt ModularRealmAuthenticator
     */
//    @Bean
//    public ModularRealmAuthenticator authenticator() {
//        ModularRealmAuthenticator authenticator = new MultiRealmAuthenticator();
//        // 设置多 Realm的认证策略，默认 AtLeastOneSuccessfulStrategy
//        AuthenticationStrategy strategy = new FirstSuccessfulStrategy();
//        authenticator.setAuthenticationStrategy(strategy);
//        return authenticator;
//    }

    // 配置org.apache.shiro.web.session.mgt.DefaultWebSessionManager(shiro session的管理)
    @Bean("sessionManager")
    public DefaultWebSessionManager getDefaultSessionManager() {
        DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
        // defaultWebSessionManager.setGlobalSessionTimeout(1000 * 60 * 60 * 24*7);// 会话过期时间，单位：毫秒(在无操作时开始计时)
        defaultWebSessionManager.setGlobalSessionTimeout(-1000L);// -1000L,永不过期 1000 * 60 * 20
        defaultWebSessionManager.setSessionValidationSchedulerEnabled(true);
        defaultWebSessionManager.setSessionIdCookieEnabled(true);
        defaultWebSessionManager.setSessionIdUrlRewritingEnabled(false);// 移除自带的JSESSIONID，方式第二次打开浏览器是进行注销操作发生
        defaultWebSessionManager.setSessionIdCookie(sessionIdCookie());
        defaultWebSessionManager.setSessionIdCookieEnabled(true);
        return defaultWebSessionManager;
    }

    /**
     * 设置cookie
     *
     * @return
     */
    private SimpleCookie sessionIdCookie() {
        SimpleCookie cookie = new SimpleCookie();
        cookie.setName("WSSESSIONID");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        return cookie;
    }

//    @Bean("sessionManager")
//    public DefaultSessionManager getDefaultSessionManager() {
//        DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
//        // defaultSessionManager.setGlobalSessionTimeout(1000 * 60 * 60 * 24*7);// 会话过期时间，单位：毫秒(在无操作时开始计时)
//        defaultSessionManager.setGlobalSessionTimeout(-1000L);// -1000L,永不过期
//        defaultSessionManager.setSessionValidationSchedulerEnabled(true);
////        defaultSessionManager.setSessionIdCookieEnabled(true);
//        return defaultSessionManager;
//    }

    /**
     * 禁用session, 不保存用户登录状态。保证每次请求都重新认证
     */
    @Bean
    protected SessionStorageEvaluator sessionStorageEvaluator() {
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        return sessionStorageEvaluator;
    }


    /**
     * 　　id：就是session id；
     * <p>
     * 　　startTimestamp：session的创建时间；
     * <p>
     * 　　stopTimestamp：session的失效时间；
     * <p>
     * 　　lastAccessTime：session的最近一次访问时间，初始值是startTimestamp
     * <p>
     * 　　timeout：session的有效时长，默认30分钟
     * <p>
     * 　　expired：session是否到期
     * <p>
     * 　　attributes：session的属性容器
     *
     * @return
     */
    // 创建一个简单的Cookie对象；创建cookie模板
    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        // 只能通过http访问cookie，js不能
        // XSS:保证该系统不会受到跨域的脚本操作攻击
        simpleCookie.setHttpOnly(true);
        //simpleCookie.setName("simpleCookie");
        //<!-- 记住我cookie生效时间30天 ,单位秒;如果设置为-1标识浏览器关闭就失效 -->
        simpleCookie.setMaxAge(2678400); // 5000   2678400
        return simpleCookie;
    }

    /**
     * cookie管理对象;
     * rememberMeManager()方法是生成rememberMe管理器，而且要将这个rememberMe管理器设置到securityManager中
     *
     * @return
     */
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

    /**
     * 开启shiro aop 注解支持. 否则注解不生效
     * 使用代理方式;所以需要开启代码支持;
     *
     * @return
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager defaultWebSecurityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(defaultWebSecurityManager);
        return authorizationAttributeSourceAdvisor;
    }

}
