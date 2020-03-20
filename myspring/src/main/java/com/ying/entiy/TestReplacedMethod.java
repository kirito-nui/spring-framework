package com.ying.entiy;

import org.springframework.beans.factory.support.MethodReplacer;

import java.lang.reflect.Method;

public class TestReplacedMethod implements MethodReplacer {
	@Override
	public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
		System.out.println("我是替代的方法");
		return null;
	}
}
