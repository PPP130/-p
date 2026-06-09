package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {

    private static final Logger log = LoggerFactory.getLogger(AutoFillAspect.class);

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        if (autoFill == null) {
            return;
        }

        OperationType operationType = autoFill.value();
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || args[0] == null) {
            return;
        }

        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            if (operationType == OperationType.INSERT) {
                invokeIfExists(entity, AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class, now);
                invokeIfExists(entity, AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
                invokeIfExists(entity, AutoFillConstant.SET_CREATE_USER, Long.class, currentId);
                invokeIfExists(entity, AutoFillConstant.SET_UPDATE_USER, Long.class, currentId);
            } else if (operationType == OperationType.UPDATE) {
                invokeIfExists(entity, AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
                invokeIfExists(entity, AutoFillConstant.SET_UPDATE_USER, Long.class, currentId);
            }
        } catch (Exception e) {
            log.error("公共字段自动填充失败", e);
        }
    }

    private void invokeIfExists(Object target, String methodName, Class<?> paramType, Object arg) throws Exception {
        Method m;
        try {
            m = target.getClass().getMethod(methodName, paramType);
        } catch (NoSuchMethodException e) {
            return;
        }
        m.invoke(target, arg);
    }
}
