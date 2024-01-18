package org.springframework.myspring.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

public class MyPointcut implements PointcutAdvisor {
	@Override
	public Advice getAdvice() {
		return null;
	}

	@Override
	public Pointcut getPointcut() {
		return null;
	}
}
