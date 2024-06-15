package com.metoo.nrsm.core.config.aop;

import com.metoo.nrsm.core.config.annotation.OperationLogAnno;
import com.metoo.nrsm.core.config.annotation.OperationType;
import com.metoo.nrsm.core.service.IOperationLogService;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.entity.OperationLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-27 14:24
 */
@Aspect
@Component
public class OperationAespect {

    public static void main(String[] args) {
        OperationType color = OperationType.CREATE;

        System.out.println("枚举常量的中文名称：" + color.getChineseName());
        System.out.println("枚举常量的中文名称：" + OperationType.getChineseName(color));

    }

    @Autowired
    private IOperationLogService operationLogService;

    @Pointcut("@annotation(com.metoo.nrsm.core.config.annotation.OperationLogAnno)")
    private void cutMethod() {
    }

    @Before("cutMethod() && @annotation(operationLogAnno)")
    public void device(JoinPoint joinPoint,
                       OperationLogAnno operationLogAnno) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        try {
            OperationLog instance = new OperationLog();
            instance.setIp(Ipv4Util.getRealIP(request));
            instance.setAction(OperationType.getChineseName(operationLogAnno.operationType()));
            instance.setDesc(operationLogAnno.name());
            instance.setType(0);
            this.operationLogService.save(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 反射获取目标类
        Class<?> targetClass = joinPoint.getTarget().getClass();

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        System.out.println(operationLogAnno.name());
        //4. 获取方法的参数 一一对应
        Object[] args = joinPoint.getArgs();
        if(args.length > 0){
            if(operationLogAnno.name().equals("")){
                System.out.println(args[0]);
            }

        }
    }

}
