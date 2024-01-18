package org.springframework.myspring.service.impl;


import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(1)
public class MyAspect1 {

	@Pointcut("execution(* org.springframework.myspring.service.impl.Louzai.everyDay())")
	private void myPointCut() {
	}

	// 前置通知
	@Before("myPointCut()")
	public void myBefore() {
		System.out.println("洗手");
	}

	// 后置通知
	@AfterReturning(value = "myPointCut()")
	public void myAfterReturning() {
		System.out.println("洗澡");
	}

}
