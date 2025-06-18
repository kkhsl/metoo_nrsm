package com.metoo.nrsm.core.config.aop.idempotent;

import com.jcraft.jsch.UserInfo;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 16:06
 *
 * 重复点击校验
 *
 */
@Slf4j
@Aspect
@Component
public class IdempotentAOP {

    /** Redis前缀 */
    private String API_IDEMPOTENT_CHECK = "API_IDEMPOTENT_CHECK:";

    @Resource
    private HttpServletRequest request;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 定义切面
     */
    @Pointcut("@annotation(com.metoo.nrsm.core.config.aop.idempotent.NotRepeat)")
    public void notRepeat() {
    }

    /**
     * 在接口原有的方法执行前，将会首先执行此处的代码
     *
     * Redis通过其事务、分布式锁、原子操作和过期时间等特性
     */
    @Before("notRepeat()")
    public void doBefore(JoinPoint joinPoint) {
        String uri = request.getRequestURI();

        // 登录后才做校验
        User loginUser = ShiroUserHolder.currentUser();
        if (loginUser != null) {
            assert uri != null;
            String key = loginUser.getUsername() + "_" + uri;
            log.info(">>>>>>>>>> 【IDEMPOTENT】开始幂等性校验，加锁，account: {}，uri: {}", loginUser.getUsername(), uri);

            // 加分布式锁
            boolean lockSuccess = redisUtils.setIfAbsent(API_IDEMPOTENT_CHECK + key, "1", 30, TimeUnit.MINUTES);
            log.info(">>>>>>>>>> 【IDEMPOTENT】分布式锁是否加锁成功:{}", lockSuccess);
            if (!lockSuccess) {
                if (uri.contains("address/pool/write")) {
                    log.error(">>>>>>>>>> 【IDEMPOTENT】配置文件保存中，请稍后");
                    throw new MyIllegalArgumentException("配置保存中，请稍后");

                } else  if (uri.contains("address/pool/ipv6/write")) {
                    log.error(">>>>>>>>>> 【IDEMPOTENT】配置文件保存中，请稍后");
                    throw new MyIllegalArgumentException("配置保存中，请稍后");
                }
            }
        }
    }

    /**
     * 在接口原有的方法执行后，都会执行此处的代码（final）
     */
    @After("notRepeat()")
    public void doAfter(JoinPoint joinPoint) {
        // 释放锁
        String uri = request.getRequestURI();
        assert uri != null;
        User loginUser = ShiroUserHolder.currentUser();
        if (loginUser != null) {
            String key = loginUser.getUsername() + "_" + uri;
            log.info(">>>>>>>>>> 【IDEMPOTENT】幂等性校验结束，释放锁，account: {}，uri: {}", loginUser.getUsername(), uri);
            redisUtils.del(API_IDEMPOTENT_CHECK + key);
        }
    }
}
