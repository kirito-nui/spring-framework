package com.ying;

import com.ying.BeanPostProcessor.BeanPostProcessorTest;
import com.ying.aop.TestAop;
import com.ying.config.AppConfig;
import com.ying.entiy.Codes;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

/**
 * @author xieyingheng
 * @title: App
 * @projectName spring
 * @description: TODO
 * @date 2020-01-2822:39
 */
public class App {

	public static void main(String[] args) {
//		AnnotationConfigApplicationContext applicationContext
//				= new AnnotationConfigApplicationContext(AppConfig.class);
//		System.out.println(applicationContext.getBean(Codes.class));

		ClassPathResource resource = new ClassPathResource("bean.xml"); // <1>
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory(); // <2>
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory); // <3>
		reader.loadBeanDefinitions(resource); // <4>

//		ApplicationContext factory = new ClassPathXmlApplicationContext("bean.xml");

//		Codes codes = (Codes) factory.getBean("myBeanFactoryInstantiation");
//		BeanPostProcessorTest beanPostProcessorTest = new BeanPostProcessorTest();
//		AbstractAutoProxyCreator abstractAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
//		factory.addBeanPostProcessor(abstractAutoProxyCreator);
//		factory.addBeanPostProcessor(beanPostProcessorTest);

//		Codes codes = (Codes) factory.getBean("codes");
////		Codes codes1 = (Codes) factory.getBean("codes");
//		System.out.println(codes);
//		System.out.println(codes.getName());
//		System.out.println(codes.getAge());
//		System.out.println(codes.getList().length);
//		System.out.println(codes.getWorker());
//		System.out.println(codes1);
//		System.out.println(codes.getWorker());

		//aop
//		TestAop testAop = (TestAop) factory.getBean("testAop");
//		testAop.TestAopMethod();
	}
}
