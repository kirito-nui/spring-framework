package com.ying.SimpleHandlerMapping;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class HandlerMappingTestMain {

	static ClassPathResource resource = new ClassPathResource("bean.xml"); // <1>
	static DefaultListableBeanFactory factory = new DefaultListableBeanFactory(); // <2>
	static XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory); // <3>
	public static void main(String[] args) {

		reader.loadBeanDefinitions(resource); // <4>
		String[] beanNamesForType = factory.getBeanNamesForType(Object.class);
		for (String beanName : beanNamesForType) {
			processCandidateBean(beanName);
		}
	}

	private static void processCandidateBean(String beanName) {
		Class<?> type = factory.getType(beanName);
		// 判断 Bean 是否为处理器，如果是，则扫描处理器方法
		if (type != null && isHandler(type)) {
			detectHandlerMethods(beanName);
		}
	}

	private static boolean isHandler(Class<?> type) {
		return hasAnnotationTest(type, Controller.class);
	}

	private static boolean hasAnnotationTest(AnnotatedElement element, Class<? extends Annotation> controllerClass) {
		return element.isAnnotationPresent(controllerClass);
	}

	private static void detectHandlerMethods(String beanName) {
		Class<?> type = factory.getType(beanName);
		Method[] declaredMethods = type.getDeclaredMethods();
		for (Method declaredMethod : declaredMethods) {
//			AnnotatedElement annotatedElement = declaredMethod;
//			System.out.println(declaredMethod.getAnnotation(TestRequestMapping.class));
//			Annotation[] declaredAnnotations = declaredMethod.getDeclaredAnnotations();
//			System.out.println(declaredAnnotations);
			TestRequestMapping declaredAnnotation = declaredMethod.getDeclaredAnnotation(TestRequestMapping.class);
			System.out.println(declaredAnnotation.value());
		}
//		TestRequestMapping declaredAnnotation = type.getDeclaredAnnotation(TestRequestMapping.class);
//		System.out.println(declaredAnnotation);
	}
}
