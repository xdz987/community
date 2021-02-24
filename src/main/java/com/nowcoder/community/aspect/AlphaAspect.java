package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    // 切入点(范围)
    // 第一个*表示方法的返回值为任意
    // 第二个*表示所有的组件，第三个*表示所有的方法，(..)表示所有的参数
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    //织入点之前
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    //织入点之后
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    //织入点返回值之后
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    //织入点报错之后
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    //织入点前后
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("around before");
        // 调用目标组件的方法
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }
}
